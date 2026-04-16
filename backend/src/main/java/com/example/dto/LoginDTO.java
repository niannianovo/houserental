package com.example.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String account;
    private String password;
    private String nickname;
    private String email;
    private String verifyCode; // 邮箱验证码
    private Integer currentRole; // 登录时选择身份：0租客 1房东
}
