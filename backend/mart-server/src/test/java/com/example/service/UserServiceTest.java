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
    @Test
    public void testRegister() {
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
        BaseContext.setCurrentId(1L);

        try {
            UserVO vo = userService.getCurrentUser();
            System.out.println("当前用户：" + vo);
        } finally {
            BaseContext.removeCurrentId();
        }
    }

    // ========== 重置密码 ==========
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

    // ========== 登出：token 写入黑名单 ==========
    @Test
    public void testLogoutAddsToBlacklist() {
        // 1. 登录拿 token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser001");
        loginDTO.setPassword("123456");
        LoginVO loginVO = userService.login(loginDTO);
        String token = loginVO.getToken();
        System.out.println("登录 token：" + token);

        // 2. 登出
        userService.logout(token);

        // 3. 检查 Redis 黑名单
        String key = String.format(RedisKeyConstant.TOKEN_BLACKLIST, token);
        Boolean exists = redisTemplate.hasKey(key);
        Long ttl = redisTemplate.getExpire(key);

        System.out.println("黑名单中存在：" + exists);
        System.out.println("黑名单 TTL（秒）：" + ttl);

        // 4. 清理（Redis 不参与事务回滚，手动删除）
        redisTemplate.delete(key);
        System.out.println("已清理黑名单 key");
    }

    // ========== 登出：Bearer 前缀正确处理 ==========
    @Test
    public void testLogoutWithBearerPrefix() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser001");
        loginDTO.setPassword("123456");
        LoginVO loginVO = userService.login(loginDTO);
        String token = loginVO.getToken();

        // 带 Bearer 前缀登出
        userService.logout("Bearer " + token);

        String key = String.format(RedisKeyConstant.TOKEN_BLACKLIST, token);
        Boolean exists = redisTemplate.hasKey(key);
        System.out.println("带 Bearer 前缀登出后，黑名单存在：" + exists);

        redisTemplate.delete(key);
    }

    // ========== 登出：无效 token 不报错 ==========
    @Test
    public void testLogoutWithInvalidToken() {
        userService.logout("invalid-token");
        System.out.println("无效 token 登出，未抛异常");
    }

    // ========== 登出：空 token 不报错 ==========
    @Test
    public void testLogoutWithEmptyToken() {
        userService.logout("");
        userService.logout(null);
        System.out.println("空 token 登出，未抛异常");
    }
}