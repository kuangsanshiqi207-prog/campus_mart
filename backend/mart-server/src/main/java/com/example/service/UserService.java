package com.example.service;

import com.example.dto.user.LoginDTO;
import com.example.dto.user.RegisterDTO;
import com.example.dto.user.ResetPasswordDTO;
import com.example.dto.user.SendCodeDTO;
import com.example.vo.user.LoginVO;
import com.example.vo.user.UserVO;

public interface UserService {

    /**
     * 发送验证码
     */
    void sendCode(SendCodeDTO dto);

    /**
     * 注册
     */
    Long register(RegisterDTO dto);

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 重置密码
     */
    void resetPassword(ResetPasswordDTO dto);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser();
}