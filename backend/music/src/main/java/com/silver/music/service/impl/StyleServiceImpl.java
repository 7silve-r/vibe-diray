package com.silver.music.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.silver.music.entity.Style;
import com.silver.music.mapper.StyleMapper;
import com.silver.music.service.StyleService;
import org.springframework.stereotype.Service;

@Service
public class StyleServiceImpl extends ServiceImpl<StyleMapper, Style> implements StyleService {}
