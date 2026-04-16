package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;
import com.example.entity.Notification;
import com.example.mapper.HouseMapper;
import com.example.service.NotificationService;
import com.example.service.VerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class VerifyServiceImpl implements VerifyService {
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    public Page<House> getPendingList(Integer page, Integer size) {
        Page<House> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getVerifyStatus, 0)
               .orderByAsc(House::getCreateTime);
        return houseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public void audit(Integer houseId, Integer adminId, Integer action, String reason) {
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }
        // action: 1通过, 2驳回
        house.setVerifyStatus(action);
        if (action == 1) {
            house.setStatus(1); // 审核通过，上架
            house.setSimilarHouseId(null); // 清除相似标记
        }
        house.setUpdateTime(new Date());
        houseMapper.updateById(house);
        log.info("【房源审核】房源ID:{}, 管理员:{}, 操作:{}, 原因:{}", houseId, adminId, action == 1 ? "通过" : "驳回", reason);

        // 通知房东审核结果
        Notification notification = new Notification();
        notification.setUserId(house.getOwnerId());
        notification.setType(2); // 审核结果
        notification.setRelatedId(houseId);
        if (action == 1) {
            notification.setTitle("房源审核通过");
            notification.setContent("您的房源【" + house.getTitle() + "】已审核通过，现已上架。");
        } else {
            notification.setTitle("房源审核未通过");
            notification.setContent("您的房源【" + house.getTitle() + "】审核未通过，原因：" + (reason != null ? reason : "无") + "。请修改后重新提交。");
        }
        notificationService.send(notification);
    }
}
