package com.example;

import com.github.xiaoymin.knife4j.spring.configuration.Knife4jAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// Knife4j 4.5 的文档增强和 SpringDoc 2.8 不兼容，文档页仍使用 Knife4j 静态资源
@SpringBootApplication(exclude = Knife4jAutoConfiguration.class)
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableDiscoveryClient   // ← 加这行
@EnableFeignClients
//@EnableCaching
public class BizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
        log.info("server started");
    }

}
