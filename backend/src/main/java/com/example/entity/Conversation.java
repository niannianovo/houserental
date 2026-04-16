package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer houseId;
    private Integer user1Id;
    private Integer user2Id;
    private Integer type;
    private String lastMessage;
    private Date lastMessageTime;
    private Date createTime;

    @TableField(exist = false)
    private String user1Nickname;
    @TableField(exist = false)
    private String user2Nickname;
}
