package com.example.service.impl.user;

import cn.hutool.core.util.RandomUtil;
import com.example.constant.JwtClaimsConstant;
import com.example.constant.MessageConstant;
import com.example.constant.RedisKeyConstant;
import com.example.constant.UserConstant;
import com.example.context.BaseContext;
import com.example.dto.user.LoginDTO;
import com.example.dto.user.RegisterDTO;
import com.example.dto.user.ResetPasswordDTO;
import com.example.dto.user.SendCodeDTO;
import com.example.entity.User;
import com.example.exception.BaseException;
import com.example.mapper.user.UserMapper;
import com.example.properties.JwtProperties;
import com.example.properties.SmsProperties;
import com.example.service.user.UserService;
import com.example.utils.JwtUtil;
import com.example.vo.user.LoginVO;
import com.example.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;
    private final SmsProperties smsProperties;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void sendCode(SendCodeDTO dto) {
        String code = RandomUtil.randomNumbers(smsProperties.getCodeLength());
        String key = String.format(RedisKeyConstant.SMS_CODE, dto.getPhone());
        redisTemplate.opsForValue().set(
                key,
                code,
                smsProperties.getExpireMinutes(),
                TimeUnit.MINUTES
        );

        // TODO 接入短信服务，MVP 阶段打日志
        log.info("【验证码】手机号 {}，验证码 {}", dto.getPhone(), code);
    }

    @Override
    @Transactional
    public Long register(RegisterDTO dto) {
        // 1. 校验验证码
        String codeKey = String.format(RedisKeyConstant.SMS_CODE, dto.getPhone());
        String cachedCode = redisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || !cachedCode.equals(dto.getCode())) {
            throw new BaseException(MessageConstant.CODE_ERROR);
        }

        // 2. 用户名是否已存在
        if (userMapper.getByUsername(dto.getUsername()) != null) {
            throw new BaseException(MessageConstant.USERNAME_EXISTS);
        }

        // 3. 手机号是否已注册
        if (userMapper.getByPhone(dto.getPhone()) != null) {
            throw new BaseException(MessageConstant.PHONE_EXISTS);
        }

        // 4. 构造用户
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(UserConstant.DEFAULT_NICKNAME_PREFIX
                        + RandomUtil.randomNumbers(UserConstant.DEFAULT_NICKNAME_RANDOM_LENGTH))
                .phone(dto.getPhone())
                .role(UserConstant.ROLE_USER)
                .status(UserConstant.STATUS_NORMAL)
                .certified(UserConstant.NOT_CERTIFIED)
                .creditScore(UserConstant.DEFAULT_CREDIT_SCORE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        userMapper.insert(user);

        // 5. 删掉验证码
        redisTemplate.delete(codeKey);

        return user.getId();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查用户
        User user = userMapper.getByUsername(dto.getUsername());
        if (user == null) {
            throw new BaseException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BaseException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        // 3. 账号状态
        if (UserConstant.STATUS_BANNED.equals(user.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_BANNED);
        }

        // 4. 生成 token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout(String token) {
        // JWT 无状态，MVP 阶段前端删 token 即可
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        // 1. 校验验证码
        String codeKey = String.format(RedisKeyConstant.SMS_CODE, dto.getPhone());
        String cachedCode = redisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || !cachedCode.equals(dto.getCode())) {
            throw new BaseException(MessageConstant.CODE_ERROR);
        }

        // 2. 查用户
        User user = userMapper.getByPhone(dto.getPhone());
        if (user == null) {
            throw new BaseException(MessageConstant.PHONE_NOT_REGISTERED);
        }

        // 3. 更新密码
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(dto.getNewPassword()));

        // 4. 删验证码
        redisTemplate.delete(codeKey);
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}