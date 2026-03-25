package com.example.controller;

import com.example.common.Result;
import com.example.entity.UserBlock;
import com.example.service.BlockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/block")
public class BlockController {
    @Autowired
    private BlockService blockService;

    @PostMapping("/{blockedUserId}")
    public Result<String> block(@RequestParam Integer userId, @PathVariable Integer blockedUserId) {
        blockService.block(userId, blockedUserId);
        return Result.success("已拉黑");
    }

    @DeleteMapping("/{blockedUserId}")
    public Result<String> unblock(@RequestParam Integer userId, @PathVariable Integer blockedUserId) {
        blockService.unblock(userId, blockedUserId);
        return Result.success("已取消拉黑");
    }

    @GetMapping("/list")
    public Result<List<UserBlock>> getBlockList(@RequestParam Integer userId) {
        return Result.success(blockService.getBlockList(userId));
    }
}
