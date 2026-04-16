package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.Appointment;
import com.example.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/appointment")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public Result<String> create(@RequestBody Appointment appointment) {
        appointmentService.create(appointment);
        return Result.success("预约成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        appointmentService.updateStatus(id, status);
        return Result.success("操作成功");
    }

    @GetMapping("/list")
    public Result<Page<Appointment>> getMyList(@RequestParam Integer userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(appointmentService.getMyList(userId, page, size));
    }

    @GetMapping("/landlord/list")
    public Result<Page<Appointment>> getLandlordList(@RequestParam Integer ownerId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(appointmentService.getLandlordList(ownerId, page, size));
    }
}
