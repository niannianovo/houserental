package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.dto.SearchResult;
import com.example.entity.House;
import com.example.service.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房源推荐控制器
 *
 * 提供三个推荐接口：
 * - /recommend/list         首页个性化推荐（偏好匹配+逐级降级+热门兜底）
 * - /recommend/search       找房页面（推荐排序+筛选条件+自动放宽+分页）
 * - /recommend/similar/{id} 房源详情页相似推荐（同城同区同类型+价格相近）
 */
@Slf4j
@RestController
@RequestMapping("/recommend")
public class RecommendController {
    @Autowired
    private RecommendService recommendService;

    /** 首页个性化推荐（不分页，默认返回10条） */
    @GetMapping("/list")
    public Result<List<House>> recommendForUser(@RequestParam Integer userId,
                                                @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(recommendService.recommendForUser(userId, limit));
    }

    /** 找房页面：推荐排序 + 用户筛选条件 + 自动放宽 + 分页 */
    @GetMapping("/search")
    public Result<SearchResult> recommendSearch(@RequestParam Integer userId,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String province,
                                               @RequestParam(required = false) String city,
                                               @RequestParam(required = false) String district,
                                               @RequestParam(required = false) Integer houseType,
                                               @RequestParam(required = false) Integer minPrice,
                                               @RequestParam(required = false) Integer maxPrice,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(recommendService.recommendWithFilter(userId, keyword, province, city,
                district, houseType, minPrice, maxPrice, page, size));
    }

    /** 房源详情页：基于当前房源属性的相似推荐（默认6条） */
    @GetMapping("/similar/{houseId}")
    public Result<List<House>> getSimilar(@PathVariable Integer houseId,
                                          @RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(recommendService.getSimilar(houseId, limit));
    }
}
