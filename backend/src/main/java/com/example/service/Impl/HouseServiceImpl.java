package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;
import com.example.entity.HouseViewLog;
import com.example.mapper.HouseMapper;
import com.example.mapper.HouseViewLogMapper;
import com.example.service.HouseService;
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

        house.setStatus(0);          // 0: 空闲
        house.setVerifyStatus(0);     // 0: 待审核
        house.setViewCount(0);
        house.setFavoriteCount(0);
        house.setContactCount(0);
        house.setCreateTime(new Date());
        house.setUpdateTime(new Date());
        houseMapper.insert(house);
        log.info("【房源发布】房东ID:{}, 房源ID:{}, 标题:{}", house.getOwnerId(), house.getId(), house.getTitle());
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

        house.setUpdateTime(new Date());
        // 修改后重新审核
        house.setVerifyStatus(0);
        houseMapper.updateById(house);
        log.info("【房源修改】房源ID:{}", house.getId());
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

        // 增加浏览量
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

        return house;
    }

    @Override
    public Page<House> search(String keyword, String address, Integer houseType,
                              Integer minPrice, Integer maxPrice, Integer page, Integer size) {
        Page<House> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();

        // 搜索审核通过和待审核的房源（排除已驳回）
        wrapper.in(House::getVerifyStatus, 0, 1);

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(House::getTitle, keyword).or().like(House::getDescription, keyword));
        }
        if (StringUtils.isNotBlank(address)) {
            wrapper.like(House::getAddress, address);
        }
        if (houseType != null) {
            wrapper.eq(House::getHouseType, houseType);
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
