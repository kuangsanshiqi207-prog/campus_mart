package com.example.gateway.filter;

import com.example.constant.JwtClaimsConstant;
import com.example.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    @Value("${campus.jwt.user-secret-key}")
    private String userSecretKey;

    /** 白名单：不需要解析 token */
    private static final String[] WHITE_LIST = {
            "/user/auth/login",
            "/user/auth/register",
            "/user/auth/send-code",
            "/user/auth/reset-password",
            "/admin/auth/login",
            "/user/market/products",
            "/user/market/categories",
            "/user/announcements"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 拦截内部接口，不对外暴露
        if (path.startsWith("/internal/")) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        // 2. 剥离前端伪造的 Header
        ServerHttpRequest.Builder builder = request.mutate()
                .headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Role");
                });

        // 3. 白名单直接放行
        if (isWhiteList(path)) {
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        // 4. 取 token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || token.isEmpty()) {
            // 没 token 让下游自己返回 401
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 5. 解析 token，把 userId 塞进 Header
        try {
            Claims claims = JwtUtil.parseJWT(userSecretKey, token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            builder.header("X-User-Id", String.valueOf(userId));
            log.debug("[Gateway] parsed userId={} for path={}", userId, path);
        } catch (Exception e) {
            log.warn("[Gateway] token 解析失败: {}", e.getMessage());
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private boolean isWhiteList(String path) {
        for (String p : WHITE_LIST) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}