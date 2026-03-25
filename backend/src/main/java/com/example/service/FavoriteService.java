package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.HouseFavorite;

public interface FavoriteService {
    void toggle(Integer userId, Integer houseId);
    boolean isFavorite(Integer userId, Integer houseId);
    Page<HouseFavorite> getMyFavorites(Integer userId, Integer page, Integer size);
}
