package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Announcement;

public interface AnnouncementService {
    void publish(Announcement announcement);
    void update(Announcement announcement);
    void delete(Integer id);
    Page<Announcement> getList(Integer status, Integer page, Integer size);
}
