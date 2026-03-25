package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import com.example.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void send(Notification notification) {
        // TODO 实现发送通知
    }

    @Override
    public void sendToAll(String title, String content, Integer type, Integer relatedId) {
        // TODO 实现群发通知
    }

    @Override
    public Page<Notification> getMyList(Integer userId, Integer page, Integer size) {
        // TODO 实现我的通知列表
        return null;
    }

    @Override
    public Integer getUnreadCount(Integer userId) {
        // TODO 实现未读数
        return 0;
    }

    @Override
    public void markAsRead(Integer id, Integer userId) {
        // TODO 实现标记已读
    }

    @Override
    public void markAllAsRead(Integer userId) {
        // TODO 实现全部已读
    }
}
