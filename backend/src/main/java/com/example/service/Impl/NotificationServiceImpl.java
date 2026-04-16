package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Notification;
import com.example.entity.User;
import com.example.mapper.NotificationMapper;
import com.example.mapper.UserMapper;
import com.example.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void send(Notification notification) {
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        notificationMapper.insert(notification);
        log.info("【通知发送】用户ID:{}, 类型:{}, 标题:{}", notification.getUserId(), notification.getType(), notification.getTitle());
    }

    @Override
    public void sendToAll(String title, String content, Integer type, Integer relatedId) {
        // 查询所有管理员
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsAdmin, 1)
               .eq(User::getStatus, 0);
        List<User> admins = userMapper.selectList(wrapper);

        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setUserId(admin.getId());
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedId(relatedId);
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            notificationMapper.insert(notification);
        }
        log.info("【群发通知】管理员数:{}, 标题:{}", admins.size(), title);
    }

    @Override
    public Page<Notification> getMyList(Integer userId, Integer page, Integer size) {
        Page<Notification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Integer getUnreadCount(Integer userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        return Math.toIntExact(notificationMapper.selectCount(wrapper));
    }

    @Override
    public void markAsRead(Integer id, Integer userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId)
               .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)
               .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
    }
}
