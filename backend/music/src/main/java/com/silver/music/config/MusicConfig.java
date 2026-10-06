package com.silver.music.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.silver.music.mapper")
public class MusicConfig {}
