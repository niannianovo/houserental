package com.example.service.Impl;

import com.example.entity.UserBlock;
import com.example.mapper.UserBlockMapper;
import com.example.service.BlockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BlockServiceImpl implements BlockService {
    @Autowired
    private UserBlockMapper userBlockMapper;

    @Override
    public void block(Integer userId, Integer blockedUserId) {
        // TODO 实现拉黑
    }

    @Override
    public void unblock(Integer userId, Integer blockedUserId) {
        // TODO 实现取消拉黑
    }

    @Override
    public boolean isBlocked(Integer userId, Integer targetUserId) {
        // TODO 实现是否被拉黑
        return false;
    }

    @Override
    public List<UserBlock> getBlockList(Integer userId) {
        // TODO 实现黑名单列表
        return null;
    }
}
