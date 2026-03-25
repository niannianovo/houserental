package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.UserReview;

public interface UserReviewService {
    void submit(UserReview review);
    Page<UserReview> getByTarget(Integer targetId, Integer page, Integer size);
    Page<UserReview> getMySent(Integer reviewerId, Integer page, Integer size);
    Page<UserReview> getMyReceived(Integer targetId, Integer page, Integer size);
}
