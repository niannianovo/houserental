package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("user_preference")
public class UserPreference {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String preferredProvince;
    private String preferredCity;
    private String preferredDistrict;
    private String preferredAreas;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Integer preferredHouseType;
    private Integer preferredRoomCount;
    private Date updateTime;
}
