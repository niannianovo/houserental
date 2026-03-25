package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.HouseFavorite;
import com.example.mapper.HouseFavoriteMapper;
import com.example.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    private HouseFavoriteMapper houseFavoriteMapper;

    @Override
    public void toggle(Integer userId, Integer houseId) {
        // TODO 实现收藏/取消收藏
    }

    @Override
    public boolean isFavorite(Integer userId, Integer houseId) {
        // TODO 实现是否已收藏
        return false;
    }

    @Override
    public Page<HouseFavorite> getMyFavorites(Integer userId, Integer page, Integer size) {
        // TODO 实现我的收藏列表
        return null;
    }
}
