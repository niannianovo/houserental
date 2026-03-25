package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.UserReview;
import com.example.mapper.UserReviewMapper;
import com.example.service.UserReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserReviewServiceImpl implements UserReviewService {
    @Autowired
    private UserReviewMapper userReviewMapper;

    @Override
    public void submit(UserReview review) {
        // TODO 实现提交互评
    }

    @Override
    public Page<UserReview> getByTarget(Integer targetId, Integer page, Integer size) {
        // TODO 实现某用户收到的评价
        return null;
    }

    @Override
    public Page<UserReview> getMySent(Integer reviewerId, Integer page, Integer size) {
        // TODO 实现我发出的评价
        return null;
    }

    @Override
    public Page<UserReview> getMyReceived(Integer targetId, Integer page, Integer size) {
        // TODO 实现我收到的评价
        return null;
    }
}
