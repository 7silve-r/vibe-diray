package com.silver.diary.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.silver.diary.mapper")
public class DiaryConfig {}
