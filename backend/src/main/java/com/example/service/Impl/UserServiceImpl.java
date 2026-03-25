package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Tool.MD5Util;
import com.example.dto.LoginDTO;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.EmailService;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailService emailService;

    @Override
    public User login(LoginDTO loginDTO) {
        log.info("【用户登录】账号:{}", loginDTO.getAccount());

        if (loginDTO.getAccount() == null) {
            throw new RuntimeException("账号不能为空");
        }
        if (loginDTO.getPassword() == null) {
            throw new RuntimeException("密码不能为空");
        }

        String encryptedPwd = MD5Util.md5(loginDTO.getPassword());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, loginDTO.getAccount())
               .eq(User::getPassword, encryptedPwd)
               .eq(User::getIsAdmin, 0);

        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 设置当前登录身份
        if (loginDTO.getCurrentRole() != null) {
            user.setCurrentRole(loginDTO.getCurrentRole());
            user.setUpdateTime(new Date());
            userMapper.updateById(user);
        }

        log.info("【登录成功】ID:{}, 账号:{}, 身份:{}", user.getId(), user.getAccount(),
                user.getCurrentRole() == 0 ? "租客" : "房东");
        user.setPassword(null);
        return user;
    }

    @Override
    public User adminLogin(LoginDTO loginDTO) {
        log.info("【管理员登录】账号:{}", loginDTO.getAccount());

        if (loginDTO.getAccount() == null || loginDTO.getPassword() == null) {
            throw new RuntimeException("账号或密码不能为空");
        }

        String encryptedPwd = MD5Util.md5(loginDTO.getPassword());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, loginDTO.getAccount())
               .eq(User::getPassword, encryptedPwd)
               .eq(User::getIsAdmin, 1);

        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("非管理员账号或密码错误");
        }

        log.info("【管理员登录成功】ID:{}", user.getId());
        user.setPassword(null);
        return user;
    }

    @Override
    public void register(LoginDTO loginDTO) {
        log.info("【用户注册】账号:{}", loginDTO.getAccount());

        // === 账号校验 ===
        if (loginDTO.getAccount() == null || loginDTO.getAccount().trim().isEmpty()) {
            throw new RuntimeException("账号不能为空");
        }
        String account = loginDTO.getAccount().trim();
        if (account.length() < 4 || account.length() > 20) {
            throw new RuntimeException("账号长度必须在4-20个字符之间");
        }
        if (!account.matches("^[a-zA-Z][a-zA-Z0-9_]{3,19}$")) {
            throw new RuntimeException("账号必须以字母开头，只能包含字母、数字和下划线");
        }

        // === 密码校验 ===
        if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        String password = loginDTO.getPassword();
        if (password.length() < 8 || password.length() > 20) {
            throw new RuntimeException("密码长度必须在8-20个字符之间");
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            throw new RuntimeException("密码必须包含至少一个字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("密码必须包含至少一个数字");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new RuntimeException("密码必须包含至少一个特殊字符（如 !@#$%^&*）");
        }

        // === 邮箱校验 ===
        if (loginDTO.getEmail() == null || loginDTO.getEmail().trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        String email = loginDTO.getEmail().trim();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new RuntimeException("邮箱格式不正确");
        }

        // === 邮箱验证码校验 ===
        if (loginDTO.getVerifyCode() == null || loginDTO.getVerifyCode().trim().isEmpty()) {
            throw new RuntimeException("请输入邮箱验证码");
        }
        if (!emailService.verifyCode(email, loginDTO.getVerifyCode().trim())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查账号是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, account);
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("账号已存在");
        }

        // 检查邮箱是否已被注册
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, email);
        if (userMapper.selectOne(emailWrapper) != null) {
            throw new RuntimeException("该邮箱已被注册");
        }

        User user = new User();
        user.setAccount(account);
        user.setPassword(MD5Util.md5(password));
        user.setEmail(email);
        user.setNickname(account);
        user.setAvatar("/avatar.png");
        user.setIsAdmin(0);
        user.setCurrentRole(0);
        user.setIsVerified(0);
        user.setIsEmailVerified(1); // 注册时已通过邮箱验证
        user.setReportCredit(100);
        user.setStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        userMapper.insert(user);
        log.info("【注册成功】账号:{}", account);
    }

    @Override
    public void updateInfo(User user) {
        // TODO 实现修改个人信息
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(MD5Util.md5(oldPassword))) {
            throw new RuntimeException("旧密码错误");
        }
        user.setPassword(MD5Util.md5(newPassword));
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    @Override
    public void forgotPassword(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }
        emailService.sendVerifyCode(email);
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        if (!emailService.verifyCode(email, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }
        user.setPassword(MD5Util.md5(newPassword));
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    @Override
    public void bindEmail(Integer userId, String email, String code) {
        if (!emailService.verifyCode(email, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setEmail(email);
        user.setIsEmailVerified(1);
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    @Override
    public User getProfile(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User switchRole(Integer userId, Integer role) {
        if (role != 0 && role != 1) {
            throw new RuntimeException("无效的角色");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setCurrentRole(role);
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }
}
