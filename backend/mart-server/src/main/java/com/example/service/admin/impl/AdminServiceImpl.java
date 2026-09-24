package com.example.service.admin.impl;

import com.example.constant.JwtClaimsConstant;
import com.example.constant.MessageConstant;
import com.example.constant.RedisKeyConstant;
import com.example.context.BaseContext;
import com.example.dto.admin.AdminLoginDTO;
import com.example.entity.Admin;
import com.example.exception.BaseException;
import com.example.mapper.admin.AdminMapper;
import com.example.properties.JwtProperties;
import com.example.service.admin.AdminService;
import com.example.utils.JwtUtil;
import com.example.vo.admin.AdminVO;
import com.example.vo.user.LoginVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginVO login(AdminLoginDTO dto) {
        Admin admin = adminMapper.getByUsername(dto.getUsername());
        if (admin == null) {
            throw new BaseException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BaseException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }
        if ("banned".equals(admin.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_BANNED);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, admin.getId());
        claims.put("type", "admin");
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );

        return LoginVO.builder()
                .token(token)
                .userId(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .avatar(admin.getAvatar())
                .role(admin.getRole())
                .build();
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) return;
        if (token.startsWith("Bearer ")) token = token.substring(7);
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Date expiration = claims.getExpiration();
            long ttlMillis = expiration.getTime() - System.currentTimeMillis();
            if (ttlMillis <= 0) return;
            String key = String.format(RedisKeyConstant.TOKEN_BLACKLIST, token);
            redisTemplate.opsForValue().set(key, "1", ttlMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("管理员登出 token 解析失败：{}", e.getMessage());
        }
    }

    @Override
    public AdminVO getCurrentAdmin() {
        Long adminId = BaseContext.getCurrentId();
        Admin admin = adminMapper.getById(adminId);
        if (admin == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }
        AdminVO vo = new AdminVO();
        BeanUtils.copyProperties(admin, vo);
        return vo;
    }
}