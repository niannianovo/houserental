package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.SearchResult;
import com.example.entity.House;

import java.util.List;

/**
 * 房源推荐服务接口
 *
 * 提供三种推荐场景：
 * 1. 首页个性化推荐（偏好逐级降级 + 热门兜底）
 * 2. 找房页面推荐搜索（推荐排序 + 筛选 + 自动放宽 + 分页）
 * 3. 详情页相似推荐（基于房源属性匹配）
 */
public interface RecommendService {
    /**
     * 首页个性化推荐
     * @param userId 用户ID（用于读取偏好）
     * @param limit  最大返回数量
     */
    List<House> recommendForUser(Integer userId, Integer limit);

    /**
     * 找房页面：推荐排序 + 筛选条件 + 自动放宽 + 分页
     * @return SearchResult 包含分页结果和放宽提示语
     */
    SearchResult recommendWithFilter(Integer userId, String keyword, String province, String city,
                                     String district, Integer houseType, Integer minPrice, Integer maxPrice,
                                     Integer page, Integer size);

    /**
     * 房源详情页相似推荐
     * @param houseId 当前房源ID
     * @param limit   最大返回数量
     */
    List<House> getSimilar(Integer houseId, Integer limit);
}
