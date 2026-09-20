package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
//@EnableCaching
public class MartServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MartServerApplication.class, args);
        log.info("server started");
    }

}
