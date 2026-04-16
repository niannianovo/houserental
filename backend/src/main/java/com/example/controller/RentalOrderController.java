package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.service.UserService;
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
    @Autowired
    private UserService userService;

    @PostMapping
    public Result<String> create(@RequestBody RentalOrder order) {
        userService.checkUserStatus(order.getTenantId());
        userService.checkUserStatus(order.getOwnerId());
        rentalOrderService.create(order);
        return Result.success("创建成功");
    }

    @GetMapping("/list")
    public Result<Page<RentalOrder>> getMyList(@RequestParam Integer userId,
                                               @RequestParam(required = false) Integer role,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(rentalOrderService.getMyList(userId, role, page, size));
    }

    @GetMapping("/history")
    public Result<Page<RentalOrder>> getHistory(@RequestParam Integer userId,
                                                @RequestParam(required = false) Integer role,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(rentalOrderService.getHistory(userId, role, page, size));
    }

    @PutMapping("/sign/{id}")
    public Result<String> sign(@PathVariable Integer id, @RequestParam Integer ownerId) {
        userService.checkUserStatus(ownerId);
        rentalOrderService.sign(id, ownerId);
        return Result.success("签约成功，等待租客缴纳押金");
    }

    @PutMapping("/reject/{id}")
    public Result<String> reject(@PathVariable Integer id, @RequestParam Integer ownerId) {
        userService.checkUserStatus(ownerId);
        rentalOrderService.reject(id, ownerId);
        return Result.success("已拒绝签约");
    }

    @PutMapping("/pay-deposit/{id}")
    public Result<String> payDeposit(@PathVariable Integer id, @RequestParam Integer tenantId) {
        userService.checkUserStatus(tenantId);
        rentalOrderService.payDeposit(id, tenantId);
        return Result.success("押金缴纳成功，租约正式生效");
    }

    @PutMapping("/refund-deposit/{id}")
    public Result<String> refundDeposit(@PathVariable Integer id, @RequestParam Integer ownerId) {
        userService.checkUserStatus(ownerId);
        rentalOrderService.refundDeposit(id, ownerId);
        return Result.success("押金已退还");
    }

    @PutMapping("/renew-apply/{id}")
    public Result<String> renewApply(@PathVariable Integer id, @RequestParam Integer tenantId, @RequestParam String newEndDate) {
        userService.checkUserStatus(tenantId);
        rentalOrderService.renewApply(id, tenantId, newEndDate);
        return Result.success("续租申请已提交");
    }

    @PutMapping("/renew-confirm/{id}")
    public Result<String> renewConfirm(@PathVariable Integer id, @RequestParam Integer ownerId) {
        userService.checkUserStatus(ownerId);
        rentalOrderService.renewConfirm(id, ownerId);
        return Result.success("续租确认成功");
    }

    @PutMapping("/quit/{id}")
    public Result<String> quitApply(@PathVariable Integer id, @RequestParam Integer operatorId) {
        userService.checkUserStatus(operatorId);
        rentalOrderService.quitApply(id, operatorId);
        return Result.success("退租申请已提交");
    }

    @PutMapping("/quit-confirm/{id}")
    public Result<String> quitConfirm(@PathVariable Integer id, @RequestParam Integer operatorId) {
        userService.checkUserStatus(operatorId);
        rentalOrderService.quitConfirm(id, operatorId);
        return Result.success("退租完成");
    }

    @PutMapping("/quit-cancel/{id}")
    public Result<String> quitCancel(@PathVariable Integer id, @RequestParam Integer operatorId) {
        rentalOrderService.quitCancel(id, operatorId);
        return Result.success("退租申请已撤回");
    }

    @PutMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Integer id, @RequestParam Integer operatorId) {
        userService.checkUserStatus(operatorId);
        rentalOrderService.cancel(id, operatorId);
        return Result.success("订单已取消");
    }
}
