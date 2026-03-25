package com.example.controller;

import com.example.common.Result;
import com.example.entity.QuickReply;
import com.example.service.QuickReplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/quick-reply")
public class QuickReplyController {
    @Autowired
    private QuickReplyService quickReplyService;

    @GetMapping("/list")
    public Result<List<QuickReply>> getList(@RequestParam Integer userId) {
        return Result.success(quickReplyService.getList(userId));
    }

    @PostMapping
    public Result<String> add(@RequestBody QuickReply quickReply) {
        quickReplyService.add(quickReply);
        return Result.success("添加成功");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody QuickReply quickReply) {
        quickReply.setId(id);
        quickReplyService.update(quickReply);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id, @RequestParam Integer userId) {
        quickReplyService.delete(id, userId);
        return Result.success("删除成功");
    }
}
