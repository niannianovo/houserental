package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("house_comment")
public class HouseComment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer houseId;
    private Integer userId;
    private String content;
    private Integer rating;
    private Integer status;
    private Date createTime;
}
