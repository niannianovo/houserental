package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Appointment;

public interface AppointmentService {
    void create(Appointment appointment);
    void updateStatus(Integer id, Integer status);
    Page<Appointment> getMyList(Integer userId, Integer page, Integer size);
}
