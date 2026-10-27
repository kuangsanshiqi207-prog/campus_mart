package com.example;

import com.github.xiaoymin.knife4j.spring.configuration.Knife4jAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = Knife4jAutoConfiguration.class)
@EnableTransactionManagement
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.api")
public class AuditApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditApplication.class, args);
        log.info("audit-service started");
    }
}
