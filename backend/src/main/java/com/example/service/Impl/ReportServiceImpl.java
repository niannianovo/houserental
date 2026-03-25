package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Report;
import com.example.mapper.ReportMapper;
import com.example.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;

    @Override
    public void submit(Report report) {
        report.setStatus(0); // 待处理
        report.setCreateTime(new Date());
        reportMapper.insert(report);
        log.info("【提交举报】举报人:{}, 目标类型:{}, 目标ID:{}", report.getReporterId(), report.getTargetType(), report.getTargetId());
    }

    @Override
    public Page<Report> getPendingList(Integer page, Integer size) {
        Page<Report> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, 0)
               .orderByAsc(Report::getCreateTime);
        return reportMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public void handle(Integer id, Integer adminId, Integer status, String result) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("举报不存在");
        }
        report.setStatus(status); // 1已处理, 2驳回
        report.setAdminId(adminId);
        report.setResult(result);
        reportMapper.updateById(report);
        log.info("【处理举报】举报ID:{}, 管理员:{}, 状态:{}", id, adminId, status);
    }
}
