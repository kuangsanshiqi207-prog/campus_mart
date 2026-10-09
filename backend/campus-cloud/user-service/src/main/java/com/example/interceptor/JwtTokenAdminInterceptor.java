package com.example.interceptor;

import com.example.constant.JwtClaimsConstant;
import com.example.constant.RedisKeyConstant;
import com.example.context.BaseContext;
import com.example.properties.JwtProperties;
import com.example.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理端 jwt 令牌校验拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从请求头取 token
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        // 2. 去掉 Bearer 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        // 3. 检查黑名单
        String blacklistKey = String.format(RedisKeyConstant.TOKEN_BLACKLIST, token);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            log.warn("管理端 token 已在黑名单中，拒绝访问");
            response.setStatus(401);
            return false;
        }

        // 4. 校验 token
        try {
            log.info("管理端 jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);

            // 校验 type 是否是 admin
            String type = claims.get("type", String.class);
            if (!"admin".equals(type)) {
                log.warn("token 类型不是 admin");
                response.setStatus(403);
                return false;
            }

            // 用 USER_ID 而不是 EMP_ID
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

            BaseContext.setCurrentId(userId);

            log.info("当前管理员id：{}", userId);
            return true;
        } catch (Exception ex) {
            log.warn("管理端 jwt 校验失败：{}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }
}