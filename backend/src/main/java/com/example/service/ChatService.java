package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.ChatMessage;
import com.example.entity.Conversation;

import java.util.List;

public interface ChatService {
    Conversation createConversation(Integer user1Id, Integer user2Id, Integer houseId, Integer type);
    List<Conversation> getMyConversations(Integer userId);
    Page<ChatMessage> getMessages(Integer conversationId, Integer page, Integer size);
    ChatMessage sendMessage(ChatMessage message);
    void markAsRead(Integer conversationId, Integer userId);
    Integer getUnreadCount(Integer userId);
}
