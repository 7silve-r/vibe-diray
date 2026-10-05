package com.silver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.silver")
@MapperScan("com.silver.music.mapper")
public class VibeApplication {
    public static void main(String[] args) {
        SpringApplication.run(VibeApplication.class, args);
    }
}
