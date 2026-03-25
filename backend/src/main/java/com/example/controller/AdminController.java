package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.HouseCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private HouseCommentService houseCommentService;
    @Autowired
    private UserMapper userMapper;

    // 用户列表
    @GetMapping("/user/list")
    public Result<Page<User>> getUserList(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsAdmin, 0); // 只查普通用户
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getAccount, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        // 隐藏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @PutMapping("/user/disable/{id}")
    public Result<String> disableUser(@PathVariable Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setStatus(1); // 1禁用
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        log.info("【禁用用户】用户ID:{}", id);
        return Result.success("已禁用");
    }

    @PutMapping("/user/enable/{id}")
    public Result<String> enableUser(@PathVariable Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setStatus(0); // 0正常
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        log.info("【启用用户】用户ID:{}", id);
        return Result.success("已启用");
    }

    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable Integer id) {
        userMapper.deleteById(id);
        log.info("【删除用户】用户ID:{}", id);
        return Result.success("已删除");
    }

    @DeleteMapping("/comment/{id}")
    public Result<String> deleteComment(@PathVariable Integer id, @RequestParam Integer adminId) {
        houseCommentService.delete(id, adminId);
        return Result.success("评论已删除");
    }
}
