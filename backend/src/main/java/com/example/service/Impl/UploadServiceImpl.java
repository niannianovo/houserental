package com.example.service.Impl;

import com.example.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class UploadServiceImpl implements UploadService {

    @Value("${file.upload-dir:D:/upload}")
    private String uploadDir;

    @Override
    public Map<String, Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String suffix = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!".jpg.jpeg.png.gif.webp".contains(suffix)) {
            throw new RuntimeException("仅支持 jpg/jpeg/png/gif/webp 格式的图片");
        }

        // 校验文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("图片大小不能超过5MB");
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 确保目录存在
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
            log.info("【文件上传成功】原始文件名:{}, 保存路径:{}", originalName, dest.getAbsolutePath());
        } catch (IOException e) {
            log.error("【文件上传失败】", e);
            throw new RuntimeException("文件上传失败");
        }

        // 返回访问路径
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("url", "/files/" + fileName);
        return result;
    }
}
