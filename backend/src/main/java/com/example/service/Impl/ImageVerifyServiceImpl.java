package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.ImageHashUtil;
import com.example.entity.House;
import com.example.entity.ImageHash;
import com.example.mapper.HouseMapper;
import com.example.mapper.ImageHashMapper;
import com.example.service.ImageVerifyService;
import com.example.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 图片相似性鉴别服务实现
 *
 * 核心功能：检测新发布房源的图片是否与已有房源图片高度相似（盗图检测）
 *
 * 采用双重检测算法：
 * 1. pHash（感知哈希）汉明距离 —— 检测整体结构相似的图片
 * 2. 颜色直方图余弦相似度     —— 检测裁剪后颜色分布仍一致的盗图
 *
 * 两种算法任一触发即标记为疑似重复，交由管理员人工审核。
 * 若均未触发，则房源自动通过审核并上架。
 */
@Service
@Slf4j
public class ImageVerifyServiceImpl implements ImageVerifyService {

    @Autowired
    private ImageHashMapper imageHashMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private NotificationService notificationService;

    @Value("${file.upload-dir:D:/upload}")
    private String uploadDir;

    /** pHash 汉明距离阈值：≤10 认为结构高度相似（64位哈希中仅10位不同） */
    private static final int PHASH_THRESHOLD = 10;
    /** 颜色直方图余弦相似度阈值：≥0.90 认为颜色分布高度一致 */
    private static final double HIST_THRESHOLD = 0.90;

    /**
     * 对房源的所有图片执行相似性鉴别
     *
     * 流程：
     * 1. 删除该房源旧的哈希记录（支持编辑后重新检测）
     * 2. 遍历房源的每张图片：
     *    a. 计算 pHash 和颜色直方图，存入 image_hash 表
     *    b. 与全库其他房源的图片逐一比对（双算法）
     *    c. 记录最小汉明距离和最大颜色相似度
     * 3. 根据检测结果决定：
     *    - 无相似 → verifyStatus=1（自动通过并上架）
     *    - 有相似 → verifyStatus=0（标记 similarHouseId，通知管理员审核）
     */
    @Override
    public void verify(House house) {
        // 重新从数据库读取最新数据，确保 images 等字段完整
        house = houseMapper.selectById(house.getId());
        if (house.getImages() == null || house.getImages().trim().isEmpty()) {
            // 无图片的房源直接自动通过
            house.setVerifyStatus(1);
            house.setStatus(1);
            houseMapper.updateById(house);
            return;
        }

        String[] imageUrls = house.getImages().split(",");

        // ---- 鉴别状态变量 ----
        Integer similarHouseId = null;        // 最相似的房源ID
        int minPhashDistance = Integer.MAX_VALUE; // 全局最小 pHash 汉明距离
        double maxHistSimilarity = 0;         // 全局最大颜色直方图相似度
        String hitMethod = null;              // 命中的检测方法描述（用于通知管理员）

        // 加载全库其他房源的图片哈希（排除当前房源自身）
        LambdaQueryWrapper<ImageHash> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(ImageHash::getHouseId, house.getId());
        List<ImageHash> allHashes = imageHashMapper.selectList(wrapper);

        // 删除该房源旧的哈希记录（编辑重新上传时需要重新计算）
        LambdaQueryWrapper<ImageHash> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(ImageHash::getHouseId, house.getId());
        imageHashMapper.delete(delWrapper);

        // ---- 逐张图片处理 ----
        for (String url : imageUrls) {
            String trimmedUrl = url.trim();
            if (trimmedUrl.isEmpty()) continue;

            // 根据URL提取文件名，拼接本地存储路径
            String fileName = trimmedUrl.substring(trimmedUrl.lastIndexOf("/") + 1);
            File imageFile = new File(uploadDir, fileName);
            if (!imageFile.exists()) {
                log.warn("【图片鉴别】图片文件不存在: {}", imageFile.getPath());
                continue;
            }

            try {
                // 计算当前图片的两种特征指纹
                long phash = ImageHashUtil.pHash(imageFile);           // 感知哈希（结构特征）
                double[] colorHist = ImageHashUtil.colorHistogram(imageFile); // 颜色直方图（颜色分布特征）

                // 将特征存入 image_hash 表，供后续新房源比对时使用
                ImageHash imageHash = new ImageHash();
                imageHash.setHouseId(house.getId());
                imageHash.setImageUrl(trimmedUrl);
                imageHash.setPhash(phash);
                imageHash.setColorHist(ImageHashUtil.histToString(colorHist));
                imageHash.setCreateTime(new Date());
                imageHashMapper.insert(imageHash);

                // ---- 与全库已有图片逐一比对（双算法） ----
                for (ImageHash existing : allHashes) {
                    // 算法1：pHash 汉明距离（检测整体结构相似）
                    int distance = ImageHashUtil.hammingDistance(phash, existing.getPhash());
                    if (distance <= PHASH_THRESHOLD && distance < minPhashDistance) {
                        minPhashDistance = distance;
                        similarHouseId = existing.getHouseId();
                        hitMethod = "pHash汉明距离:" + distance;
                    }

                    // 算法2：颜色直方图余弦相似度（检测裁剪盗图）
                    double[] existingHist = ImageHashUtil.histFromString(existing.getColorHist());
                    double similarity = ImageHashUtil.histogramSimilarity(colorHist, existingHist);
                    if (similarity >= HIST_THRESHOLD && similarity > maxHistSimilarity) {
                        maxHistSimilarity = similarity;
                        // pHash 优先级更高，只在 pHash 未命中时才采用直方图结果
                        if (minPhashDistance > PHASH_THRESHOLD) {
                            similarHouseId = existing.getHouseId();
                            hitMethod = "颜色直方图相似度:" + String.format("%.2f", similarity);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("【图片鉴别】计算失败: {}", imageFile.getPath(), e);
            }
        }

        // ---- 根据检测结果更新房源状态 ----
        boolean isSimilar = (minPhashDistance <= PHASH_THRESHOLD || maxHistSimilarity >= HIST_THRESHOLD)
                && similarHouseId != null;

        if (isSimilar) {
            // 发现相似图片 → 待人工审核
            house.setVerifyStatus(0);
            house.setStatus(0);
            house.setSimilarHouseId(similarHouseId); // 记录最相似的房源ID，供管理员对比查看
            houseMapper.updateById(house);

            // 通知所有管理员进行人工审核
            House similarHouse = houseMapper.selectById(similarHouseId);
            String similarTitle = similarHouse != null ? similarHouse.getTitle() : "ID:" + similarHouseId;
            notificationService.sendToAll(
                    "房源图片疑似重复",
                    "新房源【" + house.getTitle() + "】(ID:" + house.getId() + ")的图片与房源【"
                            + similarTitle + "】(ID:" + similarHouseId + ")高度相似（" + hitMethod + "），请人工审核。",
                    2,
                    house.getId()
            );
            log.info("【图片鉴别】房源{}与房源{}图片相似，{}", house.getId(), similarHouseId, hitMethod);
        } else {
            // 无相似图片 → 自动通过审核并上架
            house.setVerifyStatus(1);
            house.setStatus(1);
            house.setSimilarHouseId(null);
            houseMapper.updateById(house);
            log.info("【图片鉴别】房源{}图片鉴别通过，自动上架", house.getId());
        }
    }
}
