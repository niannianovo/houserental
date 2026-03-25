package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Announcement;
import com.example.mapper.AnnouncementMapper;
import com.example.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class AnnouncementServiceImpl implements AnnouncementService {
    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public void publish(Announcement announcement) {
        announcement.setStatus(1); // 1已发布
        announcement.setCreateTime(new Date());
        announcement.setUpdateTime(new Date());
        announcementMapper.insert(announcement);
        log.info("【发布公告】标题:{}", announcement.getTitle());
    }

    @Override
    public void update(Announcement announcement) {
        Announcement existing = announcementMapper.selectById(announcement.getId());
        if (existing == null) {
            throw new RuntimeException("公告不存在");
        }
        announcement.setUpdateTime(new Date());
        announcementMapper.updateById(announcement);
        log.info("【更新公告】ID:{}", announcement.getId());
    }

    @Override
    public void delete(Integer id) {
        announcementMapper.deleteById(id);
        log.info("【删除公告】ID:{}", id);
    }

    @Override
    public Page<Announcement> getList(Integer status, Integer page, Integer size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        wrapper.orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectPage(pageParam, wrapper);
    }
}
