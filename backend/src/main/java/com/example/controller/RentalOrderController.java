package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.RentalOrder;
import com.example.service.RentalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/rental-order")
public class RentalOrderController {
    @Autowired
    private RentalOrderService rentalOrderService;

    @PostMapping
    public Result<String> create(@RequestBody RentalOrder order) {
        rentalOrderService.create(order);
        return Result.success("创建成功");
    }

    @GetMapping("/list")
    public Result<Page<RentalOrder>> getMyList(@RequestParam Integer userId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(rentalOrderService.getMyList(userId, page, size));
    }

    @GetMapping("/history")
    public Result<Page<RentalOrder>> getHistory(@RequestParam Integer userId,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(rentalOrderService.getHistory(userId, page, size));
    }

    @PutMapping("/sign/{id}")
    public Result<String> sign(@PathVariable Integer id, @RequestParam Integer tenantId) {
        rentalOrderService.sign(id, tenantId);
        return Result.success("签约成功");
    }

    @PutMapping("/renew/{id}")
    public Result<String> renew(@PathVariable Integer id, @RequestParam String newEndDate) {
        rentalOrderService.renew(id, newEndDate);
        return Result.success("续租成功");
    }

    @PutMapping("/quit/{id}")
    public Result<String> quitApply(@PathVariable Integer id, @RequestParam Integer operatorId) {
        rentalOrderService.quitApply(id, operatorId);
        return Result.success("退租申请已提交");
    }

    @PutMapping("/quit-confirm/{id}")
    public Result<String> quitConfirm(@PathVariable Integer id, @RequestParam Integer operatorId) {
        rentalOrderService.quitConfirm(id, operatorId);
        return Result.success("退租完成");
    }
}
