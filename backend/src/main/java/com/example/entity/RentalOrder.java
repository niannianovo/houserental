package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
    private BigDecimal monthlyRent;
    private String contractFile;
    private Integer status;
    private BigDecimal depositAmount;
    /** 押金状态：0-未付 1-已付 2-已退 */
    private Integer depositStatus;
    /** 退租申请人ID */
    private Integer quitApplicant;
    private Date createTime;

    /** 本月应缴租金（非数据库字段，查询时计算填充） */
    @TableField(exist = false)
    private BigDecimal currentMonthRent;

    /** 房源标题（非数据库字段，列表查询时填充） */
    @TableField(exist = false)
    private String houseTitle;

    /** 房源图片，逗号分隔（非数据库字段，列表查询时填充） */
    @TableField(exist = false)
    private String houseImages;
}
