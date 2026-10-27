package com.example.config;

import com.example.interceptor.JwtTokenAdminInterceptor;
import com.example.interceptor.JwtTokenUserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ProductWebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;
    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端：拦 /user/**（无本地登录，全部校验 token）
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**");

        // 管理端：拦 /admin/**（登录在 user-service，此处全部校验 token）
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**");
    }
}
