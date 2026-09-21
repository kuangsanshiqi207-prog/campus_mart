package com.example.service;

import com.example.constant.RedisKeyConstant;
import com.example.context.BaseContext;
import com.example.dto.user.LoginDTO;
import com.example.dto.user.RegisterDTO;
import com.example.dto.user.ResetPasswordDTO;
import com.example.dto.user.SendCodeDTO;
import com.example.service.user.UserService;
import com.example.vo.user.LoginVO;
import com.example.vo.user.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@Transactional
public class UserServiceTest {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // ========== 发送验证码 ==========
    @Test
    public void testSendCode() {
        SendCodeDTO dto = new SendCodeDTO();
        dto.setPhone("13800138000");

        userService.sendCode(dto);

        String code = redisTemplate.opsForValue()
                .get(String.format(RedisKeyConstant.SMS_CODE, "13800138000"));
        System.out.println("验证码：" + code);
    }

    // ========== 注册 ==========
    // 注意：用独立用户名和手机号，避免和 testGetCurrentUser / testLogin 使用的 testuser001 冲突
    @Test
    public void testRegister() {
        // 先把验证码写进 Redis，避免依赖发送接口
        String phone = "13900000001";
        String username = "testuser_register";

        redisTemplate.opsForValue().set(
                String.format(RedisKeyConstant.SMS_CODE, phone),
                "123456",
                5,
                TimeUnit.MINUTES
        );

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword("123456");
        dto.setPhone(phone);
        dto.setCode("123456");

        Long userId = userService.register(dto);
        System.out.println("注册成功，userId：" + userId);
    }

    // ========== 登录 ==========
    @Test
    public void testLogin() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser001");
        dto.setPassword("123456");

        LoginVO vo = userService.login(dto);
        System.out.println("登录成功：");
        System.out.println("token = " + vo.getToken());
        System.out.println("userId = " + vo.getUserId());
        System.out.println("nickname = " + vo.getNickname());
    }

    // ========== 获取当前用户 ==========
    @Test
    public void testGetCurrentUser() {
        // 模拟拦截器把 userId 放进 ThreadLocal
        BaseContext.setCurrentId(1L);

        try {
            UserVO vo = userService.getCurrentUser();
            System.out.println("当前用户：" + vo);
        } finally {
            // 用 finally 保证即使抛异常也会清理 ThreadLocal，避免污染后续测试
            BaseContext.removeCurrentId();
        }
    }

    // ========== 重置密码 ==========
    // 因为类上有 @Transactional，本方法修改的密码会在方法结束后回滚，
    // 不会影响 testLogin 里 testuser001 的密码
    @Test
    public void testResetPassword() {
        String phone = "13800138001";
        redisTemplate.opsForValue().set(
                String.format(RedisKeyConstant.SMS_CODE, phone),
                "654321",
                5,
                TimeUnit.MINUTES
        );

        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setPhone(phone);
        dto.setCode("654321");
        dto.setNewPassword("abcdef");

        userService.resetPassword(dto);
        System.out.println("重置密码成功");
    }

    // ========== 登出 ==========
    @Test
    public void testLogout() {
        userService.logout("any-token");
        System.out.println("登出成功");
    }
}