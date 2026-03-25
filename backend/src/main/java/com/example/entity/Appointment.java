package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer houseId;
    private Integer userId;
    private Date appointmentTime;
    private Integer status;
    private String remark;
    private Date createTime;
}
