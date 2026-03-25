package com.example.controller;

import com.example.common.Result;
import com.example.service.DashboardService;
import com.example.vo.DashboardOverviewVO;
import com.example.vo.TrendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardOverviewVO> getOverview() {
        return Result.success(dashboardService.getOverview());
    }

    @GetMapping("/order-trend")
    public Result<List<TrendVO>> getOrderTrend(@RequestParam(defaultValue = "12") Integer months) {
        return Result.success(dashboardService.getOrderTrend(months));
    }

    @GetMapping("/payment-trend")
    public Result<List<TrendVO>> getPaymentTrend(@RequestParam(defaultValue = "12") Integer months) {
        return Result.success(dashboardService.getPaymentTrend(months));
    }
}
