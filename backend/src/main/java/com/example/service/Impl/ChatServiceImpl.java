package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.ChatMessage;
import com.example.entity.Conversation;
import com.example.mapper.ChatMessageMapper;
import com.example.mapper.ConversationMapper;
import com.example.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public Conversation createConversation(Integer user1Id, Integer user2Id, Integer houseId, Integer type) {
        // TODO 实现创建会话
        return null;
    }

    @Override
    public List<Conversation> getMyConversations(Integer userId) {
        // TODO 实现我的会话列表
        return null;
    }

    @Override
    public Page<ChatMessage> getMessages(Integer conversationId, Integer page, Integer size) {
        // TODO 实现历史消息
        return null;
    }

    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        // TODO 实现发送消息
        return null;
    }

    @Override
    public void markAsRead(Integer conversationId, Integer userId) {
        // TODO 实现标记已读
    }

    @Override
    public Integer getUnreadCount(Integer userId) {
        // TODO 实现未读数
        return 0;
    }
}
