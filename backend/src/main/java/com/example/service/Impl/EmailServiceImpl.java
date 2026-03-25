package com.example.service.Impl;

import com.example.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 验证码缓存：email -> {code, expireTime}
    private static final Map<String, String[]> codeCache = new ConcurrentHashMap<>();

    // 验证码有效期：5分钟
    private static final long CODE_EXPIRE_MS = 5 * 60 * 1000;

    @Override
    public void sendVerifyCode(String email) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 存入缓存，记录过期时间
        codeCache.put(email, new String[]{code, String.valueOf(System.currentTimeMillis() + CODE_EXPIRE_MS)});

        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("【房屋租赁系统】注册验证码");
        message.setText("您好，\n\n您的注册验证码为：" + code + "\n\n验证码有效期为5分钟，请尽快完成注册。\n\n如非本人操作，请忽略此邮件。");

        try {
            mailSender.send(message);
            log.info("【验证码发送成功】邮箱:{}, 验证码:{}", email, code);
        } catch (Exception e) {
            log.error("【验证码发送失败】邮箱:{}", email, e);
            codeCache.remove(email);
            throw new RuntimeException("验证码发送失败，请检查邮箱地址是否正确");
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String[] cached = codeCache.get(email);
        if (cached == null) {
            return false;
        }

        String savedCode = cached[0];
        long expireTime = Long.parseLong(cached[1]);

        // 检查是否过期
        if (System.currentTimeMillis() > expireTime) {
            codeCache.remove(email);
            return false;
        }

        // 验证成功后移除验证码（一次性使用）
        if (savedCode.equals(code)) {
            codeCache.remove(email);
            return true;
        }

        return false;
    }
}
