package com.example.common;

import com.example.entity.House;
import com.example.entity.User;

public class VerifyScoreCalculator {

    public static int calculate(House house, User owner, int confirmedReportCount, boolean hasHistoryFake, int recentPublishCount) {
        int score = 0;

        // 房东已实名认证 +20
        if (owner.getIsVerified() != null && owner.getIsVerified() == 1) {
            score += 20;
        }

        // 房源已认证 +20
        if (house.getIsCertified() != null && house.getIsCertified() == 1) {
            score += 20;
        }

        // 图片数量 >= 3 +15
        if (house.getImages() != null && house.getImages().split(",").length >= 3) {
            score += 15;
        }

        // 描述字数 >= 50 +10
        if (house.getDescription() != null && house.getDescription().length() >= 50) {
            score += 10;
        }

        // 联系电话格式有效 +10
        if (house.getContactPhone() != null && house.getContactPhone().matches("^1[3-9]\\d{9}$")) {
            score += 10;
        }

        // 地址信息完整 +10
        if (house.getAddress() != null && house.getAddress().length() >= 10) {
            score += 10;
        }

        // 房东历史无虚假记录 +10
        if (!hasHistoryFake) {
            score += 10;
        }

        // TODO 价格在同区域均价范围内 +15（需要查询同区域均价）

        // 扣分项
        if (confirmedReportCount >= 3) {
            score -= 20;
        }
        if (hasHistoryFake) {
            score -= 30;
        }
        if (recentPublishCount > 10) {
            score -= 10;
        }

        // 归一化到 0-100
        score = Math.max(0, Math.min(100, score));
        return score;
    }
}
