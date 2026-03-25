package com.example.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrendVO {
    private String month;
    private BigDecimal value;
}
