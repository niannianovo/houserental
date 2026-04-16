package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.UserBlock;
import com.example.mapper.UserBlockMapper;
import com.example.service.BlockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class BlockServiceImpl implements BlockService {
    @Autowired
    private UserBlockMapper userBlockMapper;

    @Override
    public void block(Integer userId, Integer blockedUserId) {
        if (userId.equals(blockedUserId)) {
            throw new RuntimeException("不能拉黑自己");
        }
        // 检查是否已拉黑
        if (isBlocked(userId, blockedUserId)) {
            throw new RuntimeException("已在黑名单中");
        }
        UserBlock block = new UserBlock();
        block.setUserId(userId);
        block.setBlockedUserId(blockedUserId);
        block.setCreateTime(new Date());
        userBlockMapper.insert(block);
        log.info("【拉黑用户】用户:{} 拉黑:{}", userId, blockedUserId);
    }

    @Override
    public void unblock(Integer userId, Integer blockedUserId) {
        LambdaQueryWrapper<UserBlock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBlock::getUserId, userId)
               .eq(UserBlock::getBlockedUserId, blockedUserId);
        userBlockMapper.delete(wrapper);
        log.info("【取消拉黑】用户:{} 取消拉黑:{}", userId, blockedUserId);
    }

    @Override
    public boolean isBlocked(Integer userId, Integer targetUserId) {
        LambdaQueryWrapper<UserBlock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBlock::getUserId, userId)
               .eq(UserBlock::getBlockedUserId, targetUserId);
        return userBlockMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<UserBlock> getBlockList(Integer userId) {
        LambdaQueryWrapper<UserBlock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBlock::getUserId, userId)
               .orderByDesc(UserBlock::getCreateTime);
        return userBlockMapper.selectList(wrapper);
    }
}
