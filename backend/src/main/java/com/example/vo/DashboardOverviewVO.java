package com.example.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardOverviewVO {
    private Integer userCount;
    private Integer houseCount;
    private Integer monthlyOrderCount;
    private BigDecimal monthlyPaymentTotal;
    private Integer pendingVerifyCount;
}
