package com.example.service;

import com.example.entity.QuickReply;

import java.util.List;

public interface QuickReplyService {
    List<QuickReply> getList(Integer userId);
    void add(QuickReply quickReply);
    void update(QuickReply quickReply);
    void delete(Integer id, Integer userId);
}
