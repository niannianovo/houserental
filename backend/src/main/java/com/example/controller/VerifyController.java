package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.House;
import com.example.service.VerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/verify")
public class VerifyController {
    @Autowired
    private VerifyService verifyService;

    @GetMapping("/pending")
    public Result<Page<House>> getPendingList(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(verifyService.getPendingList(page, size));
    }

    @PostMapping("/{houseId}")
    public Result<String> audit(@PathVariable Integer houseId,
                                @RequestParam Integer adminId,
                                @RequestParam Integer action,
                                @RequestParam(required = false) String reason) {
        verifyService.audit(houseId, adminId, action, reason);
        return Result.success("审核完成");
    }
}
