package com.example.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "campus.file")
public class FileProperties {

    /**
     * 存储类型：aliyun-oss / local
     */
    private String storageType;

    private Local local = new Local();

    @Data
    public static class Local {
        private String basePath = "./uploads";
        private String baseUrl = "http://localhost:8080/uploads";
    }
}