package com.silver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.silver")
public class VibeApplication {
    public static void main(String[] args) {
        SpringApplication.run(VibeApplication.class, args);
    }
}
