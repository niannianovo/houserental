package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("image_hash")
public class ImageHash {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer houseId;
    private String imageUrl;
    private Long phash;
    private String colorHist;
    private Date createTime;
}
