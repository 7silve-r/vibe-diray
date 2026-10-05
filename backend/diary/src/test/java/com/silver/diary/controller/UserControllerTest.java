package com.silver.diary.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.silver.diary.dto.*;
import com.silver.diary.entity.User;
import com.silver.diary.exception.BusinessException;
import com.silver.diary.service.UserService;
import com.silver.diary.support.TestData;
import com.silver.diary.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

/** 使用真实控制器，只模拟数据库和 JWT 依赖。 */
class UserControllerTest {
    UserService users;
    JwtUtil jwt;
    UserController controller;
    LambdaQueryChainWrapper<User> query;
    final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        users = mock(UserService.class);
        jwt = mock(JwtUtil.class);
        query = TestData.users(users);
        controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", users);
        ReflectionTestUtils.setField(controller, "jwtUtil", jwt);
    }

    RegisterDto reg() {
        var dto = new RegisterDto();
        dto.setUsername("writer01");
        dto.setPassword("OldPass123!");
        dto.setRePassword("OldPass123!");
        return dto;
    }

    LoginDto login() {
        var dto = new LoginDto();
        dto.setUsername("writer01");
        dto.setPassword("OldPass123!");
        return dto;
    }

    PasswordDto pwd() {
        var dto = new PasswordDto();
        dto.setOldPwd("OldPass123!");
        dto.setNewPwd("NewPass123!");
        dto.setReNewPwd("NewPass123!");
        return dto;
    }

    User account() {
        var user = TestData.user();
        user.setPassword(encoder.encode("OldPass123!"));
        when(query.one()).thenReturn(user);
        return user;
    }

    @Test
    void register() {
        when(users.save(any(User.class))).thenReturn(true);
        assertEquals(200, controller.register(reg()).getCode());
        var saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertTrue(encoder.matches("OldPass123!", saved.getValue().getPassword()));
        assertNotEquals("OldPass123!", saved.getValue().getPassword());
    }

    @Test
    void dupUser() {
        when(query.exists()).thenReturn(true);
        assertThrows(BusinessException.class, () -> controller.register(reg()));
        verify(users, never()).save(any(User.class));
    }

    @Test
    void badName() {
        var dto = reg();
        dto.setUsername("a");
        assertThrows(BusinessException.class, () -> controller.register(dto));
        verifyNoInteractions(users);
    }

    @Test
    void mismatch() {
        var dto = reg();
        dto.setRePassword("different");
        assertThrows(BusinessException.class, () -> controller.register(dto));
        verify(users, never()).save(any(User.class));
    }

    @Test
    void saveFail() {
        when(users.save(any(User.class))).thenReturn(false);
        assertThrows(BusinessException.class, () -> controller.register(reg()));
    }

    @Test
    void loginOk() {
        account();
        when(jwt.generateToken("writer01", 0)).thenReturn("signed-token");
        assertEquals("signed-token", controller.login(login()).getData());
    }

    @Test
    void badPwd() {
        account();
        var dto = login();
        dto.setPassword("wrong");
        assertThrows(BusinessException.class, () -> controller.login(dto));
        verifyNoInteractions(jwt);
    }

    @Test
    void noUser() {
        assertThrows(BusinessException.class, () -> controller.login(login()));
        verifyNoInteractions(jwt);
    }

    @Test
    void hidePwd() {
        account();
        assertNull(controller.getUserInfo("token").getData().getPassword());
    }

    @Test
    void goneUser() {
        assertEquals(
                401,
                assertThrows(ResponseStatusException.class, () -> controller.getUserInfo("token"))
                        .getStatusCode()
                        .value());
    }

    @Test
    void oldPwd() {
        account();
        var dto = pwd();
        dto.setOldPwd("wrong");
        assertThrows(BusinessException.class, () -> controller.updatePassword("token", dto));
        verify(users, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("修改密码后应保存 BCrypt 散列，而非明文")
    void hashPwd() {
        account();
        when(users.updateById(any(User.class))).thenReturn(true);
        controller.updatePassword("token", pwd());
        var saved = ArgumentCaptor.forClass(User.class);
        verify(users).updateById(saved.capture());
        assertNotEquals("NewPass123!", saved.getValue().getPassword(), "修改密码不能保存明文");
        assertTrue(encoder.matches("NewPass123!", saved.getValue().getPassword()));
    }

    @Test
    @DisplayName("确认密码缺失应返回业务异常，不能抛空指针")
    void noRepeat() {
        account();
        var dto = pwd();
        dto.setReNewPwd(null);
        assertThrows(BusinessException.class, () -> controller.updatePassword("token", dto));
        verify(users, never()).updateById(any(User.class));
    }
}
