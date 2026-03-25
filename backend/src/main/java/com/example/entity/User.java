package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String account;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer isAdmin;
    private Integer currentRole;
    private String realName;
    private String idCard;
    private Integer isVerified;
    private Integer isEmailVerified;
    private String idCardFront;
    private String idCardBack;
    private Integer reportCredit;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
