package com.silver.diary.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.silver.diary.entity.*;
import com.silver.diary.exception.BusinessException;
import com.silver.diary.service.*;
import com.silver.diary.support.TestData;
import com.silver.diary.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

class CategoryControllerTest {
    CategoryService categories;
    ArticleService articles;
    CategoryController controller;
    LambdaQueryChainWrapper<Article> query;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        categories = mock(CategoryService.class);
        articles = mock(ArticleService.class);
        var users = mock(UserService.class);
        when(TestData.users(users).one()).thenReturn(TestData.user());
        query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(query).eq(any(), any());
        when(articles.lambdaQuery()).thenReturn(query);
        controller = new CategoryController();
        ReflectionTestUtils.setField(controller, "categoryService", categories);
        ReflectionTestUtils.setField(controller, "articleService", articles);
        ReflectionTestUtils.setField(controller, "userService", users);
        ReflectionTestUtils.setField(controller, "jwtUtil", mock(JwtUtil.class));
    }

    Category category(int owner) {
        var c = new Category();
        c.setId(7);
        c.setCreateUser(owner);
        c.setCateName("生活");
        c.setCateAlias("life");
        return c;
    }

    @Test
    void addOwner() {
        var c = category(99);
        when(categories.save(any(Category.class))).thenReturn(true);
        controller.add(c, "token");
        assertNull(c.getId());
        assertEquals(1, c.getCreateUser());
    }

    @Test
    void badName() {
        var c = category(1);
        c.setCateName(" ");
        assertThrows(BusinessException.class, () -> controller.add(c, "token"));
        verifyNoInteractions(categories);
    }

    @Test
    void inUse() {
        when(categories.getById(7)).thenReturn(category(1));
        when(query.exists()).thenReturn(true);
        assertThrows(BusinessException.class, () -> controller.delete(7, "token"));
        verify(categories, never()).removeById(anyInt());
    }

    @Test
    void foreignEdit() {
        when(categories.getById(7)).thenReturn(category(2));
        assertThrows(BusinessException.class, () -> controller.update(category(1), "token"));
        verify(categories, never()).updateById(any(Category.class));
    }

    @Test
    void keepOwner() {
        var saved = category(1);
        when(categories.getById(7)).thenReturn(saved);
        when(categories.updateById(any(Category.class))).thenReturn(true);
        controller.update(category(99), "token");
        assertEquals(1, saved.getCreateUser());
    }

    @Test
    void deleteOk() {
        when(categories.getById(7)).thenReturn(category(1));
        when(categories.removeById(7)).thenReturn(true);
        assertEquals(200, controller.delete(7, "token").getCode());
    }
}
