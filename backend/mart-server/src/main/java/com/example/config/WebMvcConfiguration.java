package com.example.config;

import com.example.interceptor.JwtTokenAdminInterceptor;
import com.example.interceptor.JwtTokenUserInterceptor;
import com.example.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;


import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                        "/user/auth/register",
                        "/user/auth/login",
                        "/user/auth/send-code",
                        "/user/auth/reset-password",
                        "/user/market/products/**",
                        "/user/market/categories",
                        "/user/reviews/product/**",
                        "/user/reviews/detail/**",
                        "/user/announcements/**"
                );
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 只在本地存储时生效，OSS 模式下这个配置无害
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/" + uploadPath + "/");
        log.info("静态资源映射 /uploads/** -> {}", uploadPath);
    }

    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始拓展消息转换器...");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0,converter);
    }

    @Value("${campus.file.local.base-path:./uploads}")
    private String uploadPath;

    // ... 拦截器注册
}
