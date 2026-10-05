package com.silver.diary;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.silver.diary.common.Result;
import com.silver.diary.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = {DiaryTestApplication.class, SecurityTest.Config.class})
@ActiveProfiles("test")
class SecurityTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired JwtUtil jwt;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM `user`");
        jdbc.update(
                "INSERT INTO `user` (id, username, role) VALUES (1, 'writer01', 'USER'), (2, 'ADMIN', 'ADMIN')");
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void noToken() throws Exception {
        mvc.perform(get("/my/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void badToken() throws Exception {
        mvc.perform(get("/my/profile").header("Authorization", "bad"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authHeader() throws Exception {
        mvc.perform(get("/my/profile").header("Authorization", jwt.generateToken("writer01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("writer01"));
    }

    @Test
    void bearer() throws Exception {
        mvc.perform(
                        get("/my/profile")
                                .header("Authorization", "Bearer " + jwt.generateToken("writer01")))
                .andExpect(status().isOk());
    }

    @Test
    void forbidden() throws Exception {
        mvc.perform(get("/admin/probe").header("Authorization", jwt.generateToken("writer01")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void admin() throws Exception {
        mvc.perform(get("/admin/probe").header("Authorization", jwt.generateToken("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void logout() throws Exception {
        String token = jwt.generateToken("writer01");
        mvc.perform(post("/my/logout").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(get("/my/profile").header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void disabled() throws Exception {
        jdbc.update("UPDATE `user` SET status = 1 WHERE id = 1");
        mvc.perform(get("/my/profile").header("Authorization", jwt.generateToken("writer01")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reserved() throws Exception {
        mvc.perform(
                        post("/api/reg")
                                .contentType("application/json")
                                .content(
                                        "{\"username\":\"admin\",\"password\":\"Test12345!\",\"rePassword\":\"Test12345!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectAdmin() throws Exception {
        mvc.perform(delete("/admin/users/2").header("Authorization", jwt.generateToken("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/my/account").header("Authorization", jwt.generateToken("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class Config {
        @Bean
        Probe probe() {
            return new Probe();
        }
    }

    @RestController
    static class Probe {
        @GetMapping("/admin/probe")
        @PreAuthorize("hasRole('ADMIN')")
        public Result<Void> probe() {
            return Result.success();
        }
    }
}
