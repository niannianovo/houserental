package com.example.controller;

import com.example.common.Result;
import com.example.entity.House;
import com.example.service.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recommend")
public class RecommendController {
    @Autowired
    private RecommendService recommendService;

    @GetMapping("/list")
    public Result<List<House>> recommendForUser(@RequestParam Integer userId,
                                                @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(recommendService.recommendForUser(userId, limit));
    }

    @GetMapping("/similar/{houseId}")
    public Result<List<House>> getSimilar(@PathVariable Integer houseId,
                                          @RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(recommendService.getSimilar(houseId, limit));
    }
}
