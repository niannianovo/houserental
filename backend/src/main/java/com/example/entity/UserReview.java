package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_review")
public class UserReview {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private Integer reviewerId;
    private Integer targetId;
    private Integer reviewerRole;
    private String content;
    private Integer rating;
    private Integer status;
    private Date createTime;
}
