package com.example.service;

import com.example.vo.DashboardOverviewVO;
import com.example.vo.TrendVO;

import java.util.List;

public interface DashboardService {
    DashboardOverviewVO getOverview();
    List<TrendVO> getOrderTrend(Integer months);
    List<TrendVO> getPaymentTrend(Integer months);
}
