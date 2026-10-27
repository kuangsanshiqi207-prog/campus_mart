package com.example.config;

import com.example.interceptor.JwtTokenAdminInterceptor;
import com.example.interceptor.JwtTokenUserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class TradeWebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;
    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端：拦 /user/orders/**
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**");

        // 管理端（本阶段无管理端订单接口，仍保留以对齐其它服务）
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**");
    }
}
