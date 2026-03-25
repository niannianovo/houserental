package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer reporterId;
    private Integer targetType;
    private Integer targetId;
    private String reason;
    private Integer status;
    private Integer adminId;
    private String result;
    private Date createTime;
}
