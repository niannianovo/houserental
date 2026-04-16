package com.example.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;
import lombok.Data;

/**
 * 推荐搜索结果 DTO
 *
 * 用于找房页面的返回结果，除了分页数据外还包含放宽提示语。
 * 当精确搜索无结果、系统自动放宽条件后，hint 字段会告诉用户做了什么调整，
 * 例如："未找到完全匹配的房源，已为您扩大了价格范围，推荐以下相近房源"
 */
@Data
public class SearchResult {
    /** 分页房源数据 */
    private Page<House> page;
    /** 放宽提示语（精确匹配有结果时为 null） */
    private String hint;

    public SearchResult(Page<House> page, String hint) {
        this.page = page;
        this.hint = hint;
    }
}
