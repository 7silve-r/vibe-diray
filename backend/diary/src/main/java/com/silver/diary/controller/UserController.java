package com.silver.diary.controller;

import com.silver.diary.common.Result;
import com.silver.diary.dto.LoginDto;
import com.silver.diary.dto.PasswordDto;
import com.silver.diary.dto.ProfileDto;
import com.silver.diary.dto.RegisterDto;
import com.silver.diary.entity.User;
import com.silver.diary.exception.BusinessException;
import com.silver.diary.service.UserService;
import com.silver.diary.upload.UploadResult;
import com.silver.diary.upload.UploadService;
import com.silver.diary.utils.JwtUtil;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired private UserService userService;

    @Autowired private JwtUtil jwtUtil;

    @Autowired private UploadService uploadService;

    /** 注册 */
    @PostMapping("/api/reg")
    public Result<Void> register(@RequestBody RegisterDto dto) {
        if (dto.getUsername() == null
                || !dto.getUsername().matches("[a-zA-Z0-9_]{5,30}")
                || dto.getPassword() == null
                || dto.getPassword().length() < 8
                || dto.getPassword().length() > 64)
            throw new BusinessException("用户名需5到30位字母/数字/下划线，密码需8到64位");
        if (userService.lambdaQuery().eq(User::getUsername, dto.getUsername()).exists()) {
            throw new BusinessException("用户名已存在");
        }
        if (!dto.getPassword().equals(dto.getRePassword())) {
            throw new BusinessException("两次密码不一致");
        }
        if ("ADMIN".equalsIgnoreCase(dto.getUsername())) throw new BusinessException("该用户名不可注册");
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (!userService.save(user)) {
            throw new BusinessException("操作未完成，数据可能已变化，请刷新后重试");
        }
        return Result.success();
    }

    /** 登录 */
    @PostMapping("/api/login")
    public Result<String> login(@RequestBody LoginDto dto) {
        User user = userService.lambdaQuery().eq(User::getUsername, dto.getUsername()).one();
        if (user == null
                || dto.getPassword() == null
                || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!Integer.valueOf(0).equals(user.getStatus())) throw new BusinessException(403, "账号已停用");
        return Result.success(jwtUtil.generateToken(dto.getUsername(), user.getTokenVersion()));
    }

    /** 获取个人信息 */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my/profile")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.getUsername(token);
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在，请重新登录");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /** 更新资料 */
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/my/profile")
    public Result<Void> updateProfile(
            @RequestHeader("Authorization") String token, @RequestBody ProfileDto dto) {
        String username = jwtUtil.getUsername(token);
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在，请重新登录");
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setEmailVerified(false);
        if (!userService.updateById(user)) {
            throw new BusinessException("操作未完成，数据可能以变化，请刷新后重试");
        }
        return Result.success();
    }

    /** 更新密码 */
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/my/password")
    public Result<Void> updatePassword(
            @RequestHeader("Authorization") String token, @RequestBody PasswordDto dto) {
        String username = jwtUtil.getUsername(token);
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在，请重新登录");
        }
        if (dto.getOldPwd() == null
                || !passwordEncoder.matches(dto.getOldPwd(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (dto.getNewPwd() == null
                || dto.getNewPwd().length() < 8
                || dto.getNewPwd().length() > 64) {
            throw new BusinessException("新密码需8到64位");
        }
        if (!dto.getNewPwd().equals(dto.getReNewPwd())) {
            throw new BusinessException("两次密码不一致");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPwd()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        if (!userService.updateById(user)) {
            throw new BusinessException("操作未完成，数据可能已变化，请刷新后重试");
        }
        return Result.success();
    }

    /** 更新头像 */
    @PreAuthorize("hasRole('USER')")
    @PatchMapping(value = "/my/avatar", consumes = "multipart/form-data")
    public Result<UploadResult> updateAvatar(
            @RequestHeader("Authorization") String token, @RequestParam("file") MultipartFile file)
            throws IOException {
        return Result.success(uploadService.avatar(jwtUtil.getUsername(token), file));
    }

    @PostMapping("/my/logout")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> logout() {
        if (!userService
                .lambdaUpdate()
                .eq(User::getId, com.silver.diary.utils.SecurityUtil.userId())
                .setSql("token_version = token_version + 1")
                .update()) {
            throw new BusinessException("退出失败");
        }
        return Result.success();
    }

    @DeleteMapping("/my/account")
    @PreAuthorize("hasRole('USER') and !hasRole('ADMIN')")
    public Result<Void> delete() {
        if (!userService.removeById(com.silver.diary.utils.SecurityUtil.userId()))
            throw new BusinessException("账号注销失败");
        return Result.success();
    }
}
