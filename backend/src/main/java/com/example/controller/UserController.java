package com.example.controller;

import com.example.common.Result;
import com.example.dto.LoginDTO;
import com.example.entity.User;
import com.example.service.EmailService;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        return Result.success(user);
    }

    @PostMapping("/login/role")
    public Result<User> switchRole(@RequestParam Integer userId, @RequestParam Integer role) {
        User user = userService.switchRole(userId, role);
        return Result.success(user);
    }

    @PostMapping("/admin/login")
    public Result<User> adminLogin(@RequestBody LoginDTO loginDTO) {
        User user = userService.adminLogin(loginDTO);
        return Result.success(user);
    }

    @PostMapping("/register/send-code")
    public Result<String> sendRegisterCode(@RequestParam String email) {
        if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new RuntimeException("邮箱格式不正确");
        }
        emailService.sendVerifyCode(email);
        return Result.success("验证码已发送到邮箱");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody LoginDTO loginDTO) {
        userService.register(loginDTO);
        return Result.success("注册成功");
    }

    @PutMapping("/user/info")
    public Result<String> updateInfo(@RequestBody User user) {
        userService.updateInfo(user);
        return Result.success("修改成功");
    }

    @PutMapping("/user/password")
    public Result<String> updatePassword(@RequestParam Integer userId,
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    @PostMapping("/user/forgot-password")
    public Result<String> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return Result.success("验证码已发送");
    }

    @PutMapping("/user/reset-password")
    public Result<String> resetPassword(@RequestParam String email,
                                        @RequestParam String code,
                                        @RequestParam String newPassword) {
        userService.resetPassword(email, code, newPassword);
        return Result.success("密码重置成功");
    }

    @PostMapping("/user/bind-email")
    public Result<String> bindEmail(@RequestParam Integer userId,
                                    @RequestParam String email,
                                    @RequestParam String code) {
        userService.bindEmail(userId, email, code);
        return Result.success("邮箱绑定成功");
    }

    @GetMapping("/user/profile/{id}")
    public Result<User> getProfile(@PathVariable Integer id) {
        User user = userService.getProfile(id);
        return Result.success(user);
    }
}
