package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Notification;

public interface NotificationService {
    void send(Notification notification);
    void sendToAll(String title, String content, Integer type, Integer relatedId);
    Page<Notification> getMyList(Integer userId, Integer page, Integer size);
    Integer getUnreadCount(Integer userId);
    void markAsRead(Integer id, Integer userId);
    void markAllAsRead(Integer userId);
}
