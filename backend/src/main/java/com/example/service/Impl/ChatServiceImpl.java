package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.ChatMessage;
import com.example.entity.Conversation;
import com.example.entity.User;
import com.example.entity.House;
import com.example.mapper.ChatMessageMapper;
import com.example.mapper.ConversationMapper;
import com.example.mapper.HouseMapper;
import com.example.mapper.UserMapper;
import com.example.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Conversation createConversation(Integer user1Id, Integer user2Id, Integer houseId, Integer type) {
        // 查询房源标题
        String houseTitle = null;
        if (houseId != null) {
            House house = houseMapper.selectById(houseId);
            if (house != null) {
                houseTitle = house.getTitle();
            }
        }

        // 检查是否已存在相同双方的会话（不区分方向，同一对用户只有一个会话）
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(w1 -> w1.eq(Conversation::getUser1Id, user1Id).eq(Conversation::getUser2Id, user2Id))
                .or(w2 -> w2.eq(Conversation::getUser1Id, user2Id).eq(Conversation::getUser2Id, user1Id))
        );
        wrapper.last("LIMIT 1");
        Conversation existing = conversationMapper.selectOne(wrapper);

        if (existing != null) {
            // 会话已存在，更新关联房源并发送提示消息
            existing.setHouseId(houseId);
            conversationMapper.updateById(existing);

            if (houseTitle != null) {
                ChatMessage hint = new ChatMessage();
                hint.setConversationId(existing.getId());
                hint.setSenderId(user1Id);
                hint.setContent("我正在咨询房源：【" + houseTitle + "】");
                hint.setMsgType(0);
                hint.setIsRead(0);
                hint.setCreateTime(new Date());
                chatMessageMapper.insert(hint);

                existing.setLastMessage(hint.getContent());
                existing.setLastMessageTime(hint.getCreateTime());
                conversationMapper.updateById(existing);
            }
            return existing;
        }

        // 新建会话
        Conversation conversation = new Conversation();
        conversation.setUser1Id(user1Id);
        conversation.setUser2Id(user2Id);
        conversation.setHouseId(houseId);
        conversation.setType(type != null ? type : 0);
        conversation.setCreateTime(new Date());
        conversationMapper.insert(conversation);

        // 新会话也发送房源提示消息
        if (houseTitle != null) {
            ChatMessage hint = new ChatMessage();
            hint.setConversationId(conversation.getId());
            hint.setSenderId(user1Id);
            hint.setContent("我正在咨询房源：【" + houseTitle + "】");
            hint.setMsgType(0);
            hint.setIsRead(0);
            hint.setCreateTime(new Date());
            chatMessageMapper.insert(hint);

            conversation.setLastMessage(hint.getContent());
            conversation.setLastMessageTime(hint.getCreateTime());
            conversationMapper.updateById(conversation);
        }

        log.info("【创建会话】user1:{}, user2:{}, houseId:{}", user1Id, user2Id, houseId);
        return conversation;
    }

    @Override
    public List<Conversation> getMyConversations(Integer userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId);
        wrapper.orderByDesc(Conversation::getLastMessageTime);
        List<Conversation> list = conversationMapper.selectList(wrapper);
        fillNicknames(list);
        return list;
    }

    private void fillNicknames(List<Conversation> list) {
        // 收集所有用户ID
        Set<Integer> userIds = new HashSet<>();
        for (Conversation conv : list) {
            userIds.add(conv.getUser1Id());
            userIds.add(conv.getUser2Id());
        }
        if (userIds.isEmpty()) return;

        // 批量查询用户昵称
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, String> nicknameMap = new HashMap<>();
        for (User u : users) {
            nicknameMap.put(u.getId(), u.getNickname());
        }

        // 填充
        for (Conversation conv : list) {
            conv.setUser1Nickname(nicknameMap.getOrDefault(conv.getUser1Id(), "用户" + conv.getUser1Id()));
            conv.setUser2Nickname(nicknameMap.getOrDefault(conv.getUser2Id(), "用户" + conv.getUser2Id()));
        }
    }

    @Override
    public Page<ChatMessage> getMessages(Integer conversationId, Integer page, Integer size) {
        Page<ChatMessage> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .orderByDesc(ChatMessage::getCreateTime);
        return chatMessageMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        message.setIsRead(0);
        message.setCreateTime(new Date());
        if (message.getMsgType() == null) {
            message.setMsgType(0); // 默认文字
        }
        chatMessageMapper.insert(message);

        // 更新会话最后消息
        Conversation conversation = conversationMapper.selectById(message.getConversationId());
        if (conversation != null) {
            conversation.setLastMessage(message.getContent());
            conversation.setLastMessageTime(message.getCreateTime());
            conversationMapper.updateById(conversation);
        }

        log.info("【发送消息】会话:{}, 发送者:{}", message.getConversationId(), message.getSenderId());
        return message;
    }

    @Override
    public void markAsRead(Integer conversationId, Integer userId) {
        LambdaUpdateWrapper<ChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .ne(ChatMessage::getSenderId, userId)
               .eq(ChatMessage::getIsRead, 0)
               .set(ChatMessage::getIsRead, 1);
        chatMessageMapper.update(null, wrapper);
    }

    @Override
    public Integer getUnreadCount(Integer userId) {
        // 查出用户参与的所有会话ID
        LambdaQueryWrapper<Conversation> convWrapper = new LambdaQueryWrapper<>();
        convWrapper.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId);
        List<Conversation> conversations = conversationMapper.selectList(convWrapper);
        if (conversations.isEmpty()) {
            return 0;
        }

        List<Integer> convIds = conversations.stream().map(Conversation::getId).collect(java.util.stream.Collectors.toList());
        LambdaQueryWrapper<ChatMessage> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.in(ChatMessage::getConversationId, convIds)
                  .ne(ChatMessage::getSenderId, userId)
                  .eq(ChatMessage::getIsRead, 0);
        return Math.toIntExact(chatMessageMapper.selectCount(msgWrapper));
    }
}
