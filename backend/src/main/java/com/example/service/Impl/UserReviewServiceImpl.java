package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.User;
import com.example.entity.UserReview;
import com.example.mapper.UserMapper;
import com.example.mapper.UserReviewMapper;
import com.example.service.UserReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserReviewServiceImpl implements UserReviewService {
    @Autowired
    private UserReviewMapper userReviewMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void submit(UserReview review) {
        // 检查是否已对同一订单评价过
        LambdaQueryWrapper<UserReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserReview::getOrderId, review.getOrderId())
               .eq(UserReview::getReviewerId, review.getReviewerId());
        Long count = userReviewMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("您已对该订单评价过");
        }

        review.setStatus(0); // 0=正常
        review.setCreateTime(new Date());
        userReviewMapper.insert(review);
        log.info("【提交互评】评价者:{} 对用户:{} 订单:{} 评分:{}", review.getReviewerId(), review.getTargetId(), review.getOrderId(), review.getRating());
    }

    @Override
    public Page<UserReview> getByTarget(Integer targetId, Integer page, Integer size) {
        Page<UserReview> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserReview::getTargetId, targetId)
               .eq(UserReview::getStatus, 0)
               .orderByDesc(UserReview::getCreateTime);
        Page<UserReview> result = userReviewMapper.selectPage(pageParam, wrapper);
        fillNicknames(result.getRecords());
        return result;
    }

    @Override
    public Page<UserReview> getMySent(Integer reviewerId, Integer page, Integer size) {
        Page<UserReview> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserReview::getReviewerId, reviewerId)
               .orderByDesc(UserReview::getCreateTime);
        Page<UserReview> result = userReviewMapper.selectPage(pageParam, wrapper);
        fillNicknames(result.getRecords());
        return result;
    }

    @Override
    public Page<UserReview> getMyReceived(Integer targetId, Integer page, Integer size) {
        Page<UserReview> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserReview::getTargetId, targetId)
               .orderByDesc(UserReview::getCreateTime);
        Page<UserReview> result = userReviewMapper.selectPage(pageParam, wrapper);
        fillNicknames(result.getRecords());
        return result;
    }

    private void fillNicknames(List<UserReview> reviews) {
        if (reviews == null || reviews.isEmpty()) return;
        Set<Integer> userIds = new HashSet<>();
        for (UserReview r : reviews) {
            userIds.add(r.getReviewerId());
            userIds.add(r.getTargetId());
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, String> nicknameMap = users.stream().collect(Collectors.toMap(User::getId, User::getNickname));
        for (UserReview r : reviews) {
            r.setReviewerNickname(nicknameMap.getOrDefault(r.getReviewerId(), "用户" + r.getReviewerId()));
            r.setTargetNickname(nicknameMap.getOrDefault(r.getTargetId(), "用户" + r.getTargetId()));
        }
    }
}
