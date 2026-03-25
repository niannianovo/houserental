package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.HouseComment;
import com.example.mapper.HouseCommentMapper;
import com.example.service.HouseCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HouseCommentServiceImpl implements HouseCommentService {
    @Autowired
    private HouseCommentMapper houseCommentMapper;

    @Override
    public void add(HouseComment comment) {
        // TODO 实现添加评论
    }

    @Override
    public Page<HouseComment> getByHouse(Integer houseId, Integer page, Integer size) {
        // TODO 实现房源评论列表
        return null;
    }

    @Override
    public Page<HouseComment> getMyComments(Integer userId, Integer page, Integer size) {
        // TODO 实现我的评论
        return null;
    }

    @Override
    public void delete(Integer id, Integer adminId) {
        // TODO 实现删除评论
    }
}
