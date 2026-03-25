package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.Report;
import com.example.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @PostMapping
    public Result<String> submit(@RequestBody Report report) {
        reportService.submit(report);
        return Result.success("举报成功");
    }

    @GetMapping("/pending")
    public Result<Page<Report>> getPendingList(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(reportService.getPendingList(page, size));
    }

    @PutMapping("/handle/{id}")
    public Result<String> handle(@PathVariable Integer id,
                                 @RequestParam Integer adminId,
                                 @RequestParam Integer status,
                                 @RequestParam(required = false) String result) {
        reportService.handle(id, adminId, status, result);
        return Result.success("处理完成");
    }
}
