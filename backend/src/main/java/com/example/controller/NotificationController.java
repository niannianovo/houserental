package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.Notification;
import com.example.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<Page<Notification>> getMyList(@RequestParam Integer userId,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(notificationService.getMyList(userId, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestParam Integer userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Integer id, @RequestParam Integer userId) {
        notificationService.markAsRead(id, userId);
        return Result.success("已读");
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead(@RequestParam Integer userId) {
        notificationService.markAllAsRead(userId);
        return Result.success("全部已读");
    }
}
