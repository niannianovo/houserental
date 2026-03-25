package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("verify_request")
public class VerifyRequest {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer type;
    private String realName;
    private String idCard;
    private String idCardFront;
    private String idCardBack;
    private Integer houseId;
    private String propertyCert;
    private Integer status;
    private String rejectReason;
    private Integer adminId;
    private Date createTime;
    private Date updateTime;
}
