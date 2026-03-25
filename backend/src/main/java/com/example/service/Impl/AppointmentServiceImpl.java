package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Appointment;
import com.example.mapper.AppointmentMapper;
import com.example.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {
    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public void create(Appointment appointment) {
        // TODO 实现创建预约
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        // TODO 实现更新预约状态
    }

    @Override
    public Page<Appointment> getMyList(Integer userId, Integer page, Integer size) {
        // TODO 实现我的预约列表
        return null;
    }
}
