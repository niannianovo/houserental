package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.ChatMessage;
import com.example.entity.Conversation;
import com.example.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/conversation")
    public Result<Conversation> createConversation(@RequestParam Integer user1Id,
                                                   @RequestParam Integer user2Id,
                                                   @RequestParam(required = false) Integer houseId,
                                                   @RequestParam Integer type) {
        return Result.success(chatService.createConversation(user1Id, user2Id, houseId, type));
    }

    @GetMapping("/conversations")
    public Result<List<Conversation>> getMyConversations(@RequestParam Integer userId) {
        return Result.success(chatService.getMyConversations(userId));
    }

    @GetMapping("/messages/{conversationId}")
    public Result<Page<ChatMessage>> getMessages(@PathVariable Integer conversationId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(chatService.getMessages(conversationId, page, size));
    }

    @PutMapping("/read/{conversationId}")
    public Result<String> markAsRead(@PathVariable Integer conversationId, @RequestParam Integer userId) {
        chatService.markAsRead(conversationId, userId);
        return Result.success("已读");
    }
}
