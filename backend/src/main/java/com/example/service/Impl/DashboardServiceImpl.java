package com.example.service.Impl;

import com.example.mapper.DashboardMapper;
import com.example.service.DashboardService;
import com.example.vo.DashboardOverviewVO;
import com.example.vo.TrendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setUserCount(dashboardMapper.countUsers());
        vo.setHouseCount(dashboardMapper.countHouses());
        vo.setPendingVerifyCount(dashboardMapper.countPendingVerify());
        vo.setPendingReportCount(dashboardMapper.countPendingReport());

        // 本月订单数
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        vo.setMonthlyOrderCount(dashboardMapper.countMonthlyOrders(currentMonth));
        vo.setMonthlyPaymentTotal(BigDecimal.ZERO);
        return vo;
    }

    @Override
    public List<TrendVO> getOrderTrend(Integer months) {
        List<TrendVO> list = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String month = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            TrendVO vo = new TrendVO();
            vo.setMonth(month);
            vo.setValue(BigDecimal.valueOf(dashboardMapper.countMonthlyOrders(month)));
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<TrendVO> getPaymentTrend(Integer months) {
        // 简单返回空趋势
        List<TrendVO> list = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            TrendVO vo = new TrendVO();
            vo.setMonth(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            vo.setValue(BigDecimal.ZERO);
            list.add(vo);
        }
        return list;
    }
}
