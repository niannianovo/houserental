package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer conversationId;
    private Integer senderId;
    private String content;
    private Integer msgType;
    private Integer isRead;
    private Date createTime;
}
