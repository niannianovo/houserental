package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("rental_order_log")
public class RentalOrderLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private Integer operatorId;
    private String action;
    private String remark;
    private Date createTime;
}
