package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.VerifyRequest;
import com.example.service.VerifyRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/verify-request")
public class VerifyRequestController {
    @Autowired
    private VerifyRequestService verifyRequestService;

    @PostMapping("/identity")
    public Result<String> submitIdentity(@RequestBody VerifyRequest request) {
        verifyRequestService.submitIdentity(request);
        return Result.success("提交成功");
    }

    @PostMapping("/house/{houseId}")
    public Result<String> submitHouse(@PathVariable Integer houseId, @RequestBody VerifyRequest request) {
        request.setHouseId(houseId);
        verifyRequestService.submitHouse(request);
        return Result.success("提交成功");
    }

    @GetMapping("/my")
    public Result<Page<VerifyRequest>> getMyRequests(@RequestParam Integer userId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(verifyRequestService.getMyRequests(userId, page, size));
    }

    @GetMapping("/pending")
    public Result<Page<VerifyRequest>> getPendingList(@RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(verifyRequestService.getPendingList(page, size));
    }

    @PutMapping("/audit/{id}")
    public Result<String> audit(@PathVariable Integer id,
                                @RequestParam Integer adminId,
                                @RequestParam Integer status,
                                @RequestParam(required = false) String rejectReason) {
        verifyRequestService.audit(id, adminId, status, rejectReason);
        return Result.success("审核完成");
    }
}
