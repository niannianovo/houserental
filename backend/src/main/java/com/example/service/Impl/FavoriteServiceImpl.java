package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;
import com.example.entity.HouseFavorite;
import com.example.mapper.HouseFavoriteMapper;
import com.example.mapper.HouseMapper;
import com.example.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    private HouseFavoriteMapper houseFavoriteMapper;
    @Autowired
    private HouseMapper houseMapper;

    @Override
    public void toggle(Integer userId, Integer houseId) {
        LambdaQueryWrapper<HouseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseFavorite::getUserId, userId)
               .eq(HouseFavorite::getHouseId, houseId);
        HouseFavorite existing = houseFavoriteMapper.selectOne(wrapper);

        if (existing != null) {
            // 取消收藏
            houseFavoriteMapper.deleteById(existing.getId());
            // 房源收藏数-1
            updateFavoriteCount(houseId, -1);
            log.info("【取消收藏】用户:{} 房源:{}", userId, houseId);
        } else {
            // 添加收藏
            HouseFavorite favorite = new HouseFavorite();
            favorite.setUserId(userId);
            favorite.setHouseId(houseId);
            favorite.setCreateTime(new Date());
            houseFavoriteMapper.insert(favorite);
            // 房源收藏数+1
            updateFavoriteCount(houseId, 1);
            log.info("【添加收藏】用户:{} 房源:{}", userId, houseId);
        }
    }

    @Override
    public boolean isFavorite(Integer userId, Integer houseId) {
        LambdaQueryWrapper<HouseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseFavorite::getUserId, userId)
               .eq(HouseFavorite::getHouseId, houseId);
        return houseFavoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Page<HouseFavorite> getMyFavorites(Integer userId, Integer page, Integer size) {
        Page<HouseFavorite> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<HouseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseFavorite::getUserId, userId)
               .orderByDesc(HouseFavorite::getCreateTime);
        Page<HouseFavorite> result = houseFavoriteMapper.selectPage(pageParam, wrapper);
        fillHouseInfo(result.getRecords());
        return result;
    }

    private void fillHouseInfo(List<HouseFavorite> favorites) {
        if (favorites == null || favorites.isEmpty()) return;
        Set<Integer> houseIds = favorites.stream().map(HouseFavorite::getHouseId).collect(Collectors.toSet());
        List<House> houses = houseMapper.selectBatchIds(houseIds);
        Map<Integer, House> houseMap = houses.stream().collect(Collectors.toMap(House::getId, h -> h));
        for (HouseFavorite fav : favorites) {
            House h = houseMap.get(fav.getHouseId());
            if (h != null) {
                fav.setHouseTitle(h.getTitle());
                fav.setHousePrice(h.getPrice());
                fav.setHouseImages(h.getImages());
                fav.setHouseAddress(h.getAddress());
                fav.setHouseDistrict(h.getDistrict());
                fav.setRoomCount(h.getRoomCount());
                fav.setHallCount(h.getHallCount());
                fav.setHouseArea(h.getArea());
                fav.setHouseType(h.getHouseType());
            }
        }
    }

    private void updateFavoriteCount(Integer houseId, int delta) {
        House house = houseMapper.selectById(houseId);
        if (house != null) {
            int newCount = (house.getFavoriteCount() != null ? house.getFavoriteCount() : 0) + delta;
            if (newCount < 0) newCount = 0;
            LambdaUpdateWrapper<House> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(House::getId, houseId)
                         .set(House::getFavoriteCount, newCount);
            houseMapper.update(null, updateWrapper);
        }
    }
}
