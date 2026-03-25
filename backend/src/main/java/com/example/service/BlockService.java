package com.example.service;

import com.example.entity.UserBlock;

import java.util.List;

public interface BlockService {
    void block(Integer userId, Integer blockedUserId);
    void unblock(Integer userId, Integer blockedUserId);
    boolean isBlocked(Integer userId, Integer targetUserId);
    List<UserBlock> getBlockList(Integer userId);
}
