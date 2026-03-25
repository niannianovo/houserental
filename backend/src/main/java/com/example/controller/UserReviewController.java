package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.UserReview;
import com.example.service.UserReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user-review")
public class UserReviewController {
    @Autowired
    private UserReviewService userReviewService;

    @PostMapping
    public Result<String> submit(@RequestBody UserReview review) {
        userReviewService.submit(review);
        return Result.success("评价成功");
    }

    @GetMapping("/{userId}")
    public Result<Page<UserReview>> getByTarget(@PathVariable Integer userId,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userReviewService.getByTarget(userId, page, size));
    }

    @GetMapping("/my-sent")
    public Result<Page<UserReview>> getMySent(@RequestParam Integer reviewerId,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userReviewService.getMySent(reviewerId, page, size));
    }

    @GetMapping("/my-received")
    public Result<Page<UserReview>> getMyReceived(@RequestParam Integer targetId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userReviewService.getMyReceived(targetId, page, size));
    }
}
