package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("house_favorite")
public class HouseFavorite {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer houseId;
    private Date createTime;

    @TableField(exist = false)
    private String houseTitle;
    @TableField(exist = false)
    private BigDecimal housePrice;
    @TableField(exist = false)
    private String houseImages;
    @TableField(exist = false)
    private String houseAddress;
    @TableField(exist = false)
    private String houseDistrict;
    @TableField(exist = false)
    private Integer roomCount;
    @TableField(exist = false)
    private Integer hallCount;
    @TableField(exist = false)
    private BigDecimal houseArea;
    @TableField(exist = false)
    private Integer houseType;
}
