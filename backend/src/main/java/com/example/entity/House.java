package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("house")
public class House {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer ownerId;
    private String title;
    private String description;
    private String province;
    private String city;
    private String district;
    private String address;
    private BigDecimal area;
    private BigDecimal price;
    private BigDecimal deposit;
    private Integer roomCount;
    private Integer hallCount;
    private String floor;
    private Integer houseType;
    private Integer status;
    private Integer verifyStatus;
    private String images;
    private String contactPhone;
    private String contactName;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer contactCount;
    private Integer similarHouseId;
    private Date createTime;
    private Date updateTime;
}
