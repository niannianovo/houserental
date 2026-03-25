package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;
import com.example.mapper.HouseMapper;
import com.example.service.VerifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class VerifyServiceImpl implements VerifyService {
    @Autowired
    private HouseMapper houseMapper;

    @Override
    public int calculateScore(House house) {
        int score = 0;
        if (StringUtils.isNotBlank(house.getTitle())) score += 10;
        if (StringUtils.isNotBlank(house.getDescription())) score += 10;
        if (StringUtils.isNotBlank(house.getAddress())) score += 10;
        if (house.getPrice() != null) score += 10;
        if (house.getArea() != null) score += 10;
        if (StringUtils.isNotBlank(house.getImages())) score += 20;
        if (StringUtils.isNotBlank(house.getContactPhone())) score += 10;
        if (StringUtils.isNotBlank(house.getContactName())) score += 10;
        if (house.getRoomCount() != null) score += 5;
        if (house.getHallCount() != null) score += 5;
        return score;
    }

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
        house.setVerifyScore(calculateScore(house));
        house.setUpdateTime(new Date());
        houseMapper.updateById(house);
        log.info("【房源审核】房源ID:{}, 管理员:{}, 操作:{}, 原因:{}", houseId, adminId, action == 1 ? "通过" : "驳回", reason);
    }
}
