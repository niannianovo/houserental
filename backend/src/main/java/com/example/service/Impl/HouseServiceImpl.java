package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;
import com.example.entity.HouseViewLog;
import com.example.entity.User;
import com.example.mapper.HouseMapper;
import com.example.mapper.HouseViewLogMapper;
import com.example.mapper.UserMapper;
import com.example.service.HouseService;
import com.example.service.ImageVerifyService;
import com.example.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class HouseServiceImpl implements HouseService {
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private HouseViewLogMapper houseViewLogMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ImageVerifyService imageVerifyService;

    @Override
    public void publish(House house) {
        if (StringUtils.isBlank(house.getTitle())) {
            throw new RuntimeException("房源标题不能为空");
        }
        if (house.getPrice() == null) {
            throw new RuntimeException("月租不能为空");
        }
        if (house.getOwnerId() == null) {
            throw new RuntimeException("房东ID不能为空");
        }

        house.setStatus(0);
        house.setVerifyStatus(0);
        house.setViewCount(0);
        house.setFavoriteCount(0);
        house.setContactCount(0);
        house.setCreateTime(new Date());
        house.setUpdateTime(new Date());
        houseMapper.insert(house);
        log.info("【房源发布】房东ID:{}, 房源ID:{}, 标题:{}", house.getOwnerId(), house.getId(), house.getTitle());

        // 图片相似性鉴别：无相似自动通过，有相似待人工审核（通知由鉴别服务发送）
        imageVerifyService.verify(house);
    }

    @Override
    public void update(House house) {
        House existing = houseMapper.selectById(house.getId());
        if (existing == null) {
            throw new RuntimeException("房源不存在");
        }
        if (!existing.getOwnerId().equals(house.getOwnerId())) {
            throw new RuntimeException("无权修改该房源");
        }
        if (existing.getStatus() == 3) {
            throw new RuntimeException("已出租的房源不允许编辑");
        }

        // 仅下架操作（只传了status=2），不触发图片鉴别
        if (house.getStatus() != null && house.getStatus() == 2 && house.getTitle() == null) {
            house.setUpdateTime(new Date());
            houseMapper.updateById(house);
            log.info("【房源下架】房源ID:{}", house.getId());
            return;
        }

        if (existing.getStatus() == 1) {
            throw new RuntimeException("已上架的房源请先下架再编辑");
        }

        house.setUpdateTime(new Date());
        house.setVerifyStatus(0);
        houseMapper.updateById(house);
        log.info("【房源修改】房源ID:{}", house.getId());

        // 重新进行图片相似性鉴别
        imageVerifyService.verify(house);
    }

    @Override
    public void delete(Integer id, Integer ownerId) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }
        if (!house.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("无权删除该房源");
        }

        houseMapper.deleteById(id);
        log.info("【房源删除】房源ID:{}, 房东ID:{}", id, ownerId);
    }

    @Override
    public House getDetail(Integer id, Integer userId) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }

        // 增加浏览量：排除房东本人和管理员
        boolean shouldCount = true;
        if (userId != null) {
            if (userId.equals(house.getOwnerId())) {
                shouldCount = false;
            } else {
                User user = userMapper.selectById(userId);
                if (user != null && user.getIsAdmin() != null && user.getIsAdmin() == 1) {
                    shouldCount = false;
                }
            }
        }

        if (shouldCount) {
            house.setViewCount(house.getViewCount() == null ? 1 : house.getViewCount() + 1);
            houseMapper.updateById(house);

            // 记录浏览日志
            if (userId != null) {
                HouseViewLog viewLog = new HouseViewLog();
                viewLog.setHouseId(id);
                viewLog.setUserId(userId);
                viewLog.setCreateTime(new Date());
                houseViewLogMapper.insert(viewLog);
            }
        }

        return house;
    }

    @Override
    public Page<House> search(Integer id, String keyword, String address, String province, String city, String district,
                              Integer houseType, Integer roomCount, Integer hallCount,
                              Integer minPrice, Integer maxPrice, Integer status,
                              Integer verifyStatus,
                              Integer page, Integer size) {
        Page<House> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();

        if (id != null) {
            wrapper.eq(House::getId, id);
            return houseMapper.selectPage(pageParam, wrapper);
        }

        // 指定了状态则按指定状态查；都没指定默认只查已上架（租客视角）
        // status=-1 表示查所有状态（管理员视角）
        if (status != null && status != -1) {
            wrapper.eq(House::getStatus, status);
        } else if (status == null && verifyStatus == null) {
            wrapper.eq(House::getStatus, 1);
        }
        if (verifyStatus != null) {
            wrapper.eq(House::getVerifyStatus, verifyStatus);
        }

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(House::getTitle, keyword).or().like(House::getDescription, keyword));
        }
        if (StringUtils.isNotBlank(province)) {
            wrapper.eq(House::getProvince, province);
        }
        if (StringUtils.isNotBlank(city)) {
            wrapper.eq(House::getCity, city);
        }
        if (StringUtils.isNotBlank(district)) {
            wrapper.eq(House::getDistrict, district);
        }
        if (StringUtils.isNotBlank(address)) {
            wrapper.like(House::getAddress, address);
        }
        if (houseType != null) {
            wrapper.eq(House::getHouseType, houseType);
        }
        if (roomCount != null) {
            wrapper.eq(House::getRoomCount, roomCount);
        }
        if (hallCount != null) {
            wrapper.eq(House::getHallCount, hallCount);
        }
        if (minPrice != null) {
            wrapper.ge(House::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(House::getPrice, maxPrice);
        }

        wrapper.orderByDesc(House::getCreateTime);
        return houseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Page<House> getMyHouses(Integer ownerId, Integer page, Integer size) {
        Page<House> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getOwnerId, ownerId)
               .orderByDesc(House::getCreateTime);
        return houseMapper.selectPage(pageParam, wrapper);
    }
}
