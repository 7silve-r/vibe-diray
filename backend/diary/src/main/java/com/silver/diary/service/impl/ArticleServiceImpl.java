package com.silver.diary.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.silver.diary.entity.Article;
import com.silver.diary.mapper.ArticleMapper;
import com.silver.diary.service.ArticleService;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleService {}
