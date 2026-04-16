package com.example.service;

import com.example.entity.House;

public interface ImageVerifyService {
    /**
     * 对房源图片进行相似性鉴别
     * 计算pHash并与全库比对，返回最相似的房源ID（无相似则返回null）
     * 同时更新 house 的 verifyStatus：
     *   无相似 → verifyStatus=1（自动通过）
     *   有相似 → verifyStatus=0（待人工审核）
     */
    void verify(House house);
}
