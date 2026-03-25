package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("rental_order")
public class RentalOrder {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer houseId;
    private Integer tenantId;
    private Integer ownerId;
    private Date startDate;
    private Date endDate;
    private BigDecimal monthlyRent;
    private String contractFile;
    private Integer status;
    private Date createTime;
}
