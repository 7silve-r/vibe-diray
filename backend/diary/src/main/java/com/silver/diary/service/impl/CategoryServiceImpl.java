package com.silver.diary.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.silver.diary.entity.Category;
import com.silver.diary.mapper.CategoryMapper;
import com.silver.diary.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {}
