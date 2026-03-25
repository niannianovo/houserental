package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("rent_payment")
public class RentPayment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private BigDecimal amount;
    private String payMonth;
    private Integer status;
    private Integer payMethod;
    private String payProof;
    private Integer confirmStatus;
    private Date payTime;
    private Date createTime;
}
