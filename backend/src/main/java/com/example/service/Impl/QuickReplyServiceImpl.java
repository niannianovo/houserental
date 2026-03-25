package com.example.service.Impl;

import com.example.entity.QuickReply;
import com.example.mapper.QuickReplyMapper;
import com.example.service.QuickReplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class QuickReplyServiceImpl implements QuickReplyService {
    @Autowired
    private QuickReplyMapper quickReplyMapper;

    @Override
    public List<QuickReply> getList(Integer userId) {
        // TODO 实现快捷回复列表
        return null;
    }

    @Override
    public void add(QuickReply quickReply) {
        // TODO 实现新增快捷回复
    }

    @Override
    public void update(QuickReply quickReply) {
        // TODO 实现修改快捷回复
    }

    @Override
    public void delete(Integer id, Integer userId) {
        // TODO 实现删除快捷回复
    }
}
