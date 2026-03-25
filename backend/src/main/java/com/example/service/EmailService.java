package com.example.service;

public interface EmailService {
    void sendVerifyCode(String email);
    boolean verifyCode(String email, String code);
}
