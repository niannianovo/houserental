package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.HouseComment;
import com.example.service.HouseCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/house-comment")
public class HouseCommentController {
    @Autowired
    private HouseCommentService houseCommentService;

    @PostMapping
    public Result<String> add(@RequestBody HouseComment comment) {
        houseCommentService.add(comment);
        return Result.success("评论成功");
    }

    @GetMapping("/list/{houseId}")
    public Result<Page<HouseComment>> getByHouse(@PathVariable Integer houseId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(houseCommentService.getByHouse(houseId, page, size));
    }

    @GetMapping("/my")
    public Result<Page<HouseComment>> getMyComments(@RequestParam Integer userId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(houseCommentService.getMyComments(userId, page, size));
    }
}
