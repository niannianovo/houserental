package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Report;

public interface ReportService {
    void submit(Report report);
    Page<Report> getPendingList(Integer page, Integer size);
    void handle(Integer id, Integer adminId, Integer status, String result);
}
