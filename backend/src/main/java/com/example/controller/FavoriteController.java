package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.HouseFavorite;
import com.example.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{houseId}")
    public Result<String> toggle(@RequestParam Integer userId, @PathVariable Integer houseId) {
        favoriteService.toggle(userId, houseId);
        return Result.success("操作成功");
    }

    @GetMapping("/list")
    public Result<Page<HouseFavorite>> getMyFavorites(@RequestParam Integer userId,
                                                      @RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(favoriteService.getMyFavorites(userId, page, size));
    }
}
