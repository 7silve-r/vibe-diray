package com.silver.diary.controller;

import com.silver.diary.common.Result;
import com.silver.diary.entity.Article;
import com.silver.diary.entity.Category;
import com.silver.diary.entity.User;
import com.silver.diary.exception.BusinessException;
import com.silver.diary.service.ArticleService;
import com.silver.diary.service.CategoryService;
import com.silver.diary.service.UserService;
import com.silver.diary.utils.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping("/my/categories")
public class CategoryController {
    @Autowired private CategoryService categoryService;
    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private ArticleService articleService;

    @GetMapping
    public Result<List<Category>> list(@RequestHeader("Authorization") String token) {
        User user =
                userService.lambdaQuery().eq(User::getUsername, jwtUtil.getUsername(token)).one();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在，请重新登录");
        }
        Integer userId = user.getId();
        return Result.success(
                categoryService.lambdaQuery().eq(Category::getCreateUser, userId).list());
    }

    @PostMapping
    public Result<Void> add(
            @RequestBody Category category, @RequestHeader("Authorization") String token) {
        if (!valid(category)) throw new BusinessException("分类名称和别名须为1到50字");
        category.setId(null);
        String username = jwtUtil.getUsername(token);
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在，请重新登陆");
        }
        category.setCreateUser(user.getId());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        if (!categoryService.save(category)) {
            throw new BusinessException("操作未完成，数据可能已变化，请刷新后重试");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(
            @RequestBody Category category, @RequestHeader("Authorization") String token) {
        Category existing =
                category.getId() == null ? null : categoryService.getById(category.getId());
        User user =
                userService.lambdaQuery().eq(User::getUsername, jwtUtil.getUsername(token)).one();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在，请重新登录");
        }
        Integer userId = user.getId();
        if (existing == null || !userId.equals(existing.getCreateUser())) {
            throw new BusinessException("分类不存在或无权访问");
        }
        if (!valid(category)) {
            throw new BusinessException("分类名称和别名须为1到50字");
        }
        existing.setCateName(category.getCateName());
        existing.setCateAlias(category.getCateAlias());
        category = existing;
        category.setUpdateTime(LocalDateTime.now());
        if (!categoryService.updateById(category)) {
            throw new BusinessException("操作未完成，数据可能已变化，请刷新后重试");
        }
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> delete(
            @RequestParam Integer id, @RequestHeader("Authorization") String token) {
        Category existing = categoryService.getById(id);
        User user =
                userService.lambdaQuery().eq(User::getUsername, jwtUtil.getUsername(token)).one();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在，请重新登录");
        }
        Integer userId = user.getId();
        if (existing == null || !userId.equals(existing.getCreateUser())) {
            throw new BusinessException("分类不存在或无权访问");
        }
        if (articleService.lambdaQuery().eq(Article::getCateId, id).exists()) {
            throw new BusinessException("请先移走该分类下的日记");
        }
        if (!categoryService.removeById(id)) {
            throw new BusinessException("操作未完成，数据可能已变化，请刷新后重试");
        }
        return Result.success();
    }

    private boolean valid(Category category) {
        return category.getCateName() != null
                && !category.getCateName().isBlank()
                && category.getCateName().length() <= 50
                && category.getCateAlias() != null
                && !category.getCateAlias().isBlank()
                && category.getCateAlias().length() <= 50;
    }
}
