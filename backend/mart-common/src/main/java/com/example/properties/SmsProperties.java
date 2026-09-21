package com.example.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信验证码配置
 */
@Component
@ConfigurationProperties(prefix = "campus.sms")
@Data
public class SmsProperties {

    /**
     * 验证码长度
     */
    private Integer codeLength = 6;

    /**
     * 验证码过期时间（分钟）
     */
    private Integer expireMinutes = 5;
}