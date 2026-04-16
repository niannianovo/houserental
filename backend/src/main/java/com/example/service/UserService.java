package com.example.service;

import com.example.dto.LoginDTO;
import com.example.entity.User;

public interface UserService {
    User login(LoginDTO loginDTO);
    User adminLogin(LoginDTO loginDTO);
    void register(LoginDTO loginDTO);
    void updateInfo(User user);
    void updatePassword(Integer userId, String oldPassword, String newPassword);
    void forgotPassword(String email);
    void resetPassword(String email, String code, String newPassword);
    void bindEmail(Integer userId, String email, String code);
    User getProfile(Integer userId);
    User switchRole(Integer userId, Integer role);
    void checkUserStatus(Integer userId);
}
