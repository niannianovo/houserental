package com.example.controller;

import com.example.common.Result;
import com.example.entity.RentPayment;
import com.example.service.RentPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rent-payment")
public class RentPaymentController {
    @Autowired
    private RentPaymentService rentPaymentService;

    @GetMapping("/list/{orderId}")
    public Result<List<RentPayment>> getList(@PathVariable Integer orderId) {
        return Result.success(rentPaymentService.getListByOrder(orderId));
    }

    @PostMapping("/pay/{id}")
    public Result<String> pay(@PathVariable Integer id,
                              @RequestParam Integer payMethod,
                              @RequestParam(required = false) String payProof) {
        rentPaymentService.pay(id, payMethod, payProof);
        return Result.success("支付成功");
    }

    @PutMapping("/confirm/{id}")
    public Result<String> confirm(@PathVariable Integer id, @RequestParam Integer ownerId) {
        rentPaymentService.confirm(id, ownerId);
        return Result.success("确认收款成功");
    }
}
