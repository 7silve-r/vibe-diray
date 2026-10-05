package com.silver.diary;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.silver.diary.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ArticleSocialTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired JwtUtil jwt;
    @Autowired com.silver.diary.mapper.ArticleMapper articles;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM article_comment_like");
        jdbc.update("DELETE FROM article_comment");
        jdbc.update("DELETE FROM article_like");
        jdbc.update("DELETE FROM article_favorite");
        jdbc.update("DELETE FROM article");
        jdbc.update("DELETE FROM category");
        jdbc.update("DELETE FROM `user`");
        jdbc.update(
                "INSERT INTO `user` (id, username, role) VALUES (1, 'owner01', 'USER'), (2, 'reader01', 'USER'), (3, 'ADMIN', 'ADMIN')");
        jdbc.update("INSERT INTO category (id, cate_name, create_user) VALUES (1, '生活', 1)");
        jdbc.update(
                "INSERT INTO article (id, title, content, state, create_user, cate_id) VALUES (10, '公开标题', '公开正文', '公开', 1, 1), (11, '私有标题', '私有正文', '私有', 1, 1)");
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    String token(String user) {
        return jwt.generateToken(user);
    }

    @Test
    void publicList() throws Exception {
        mvc.perform(get("/public/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(10));
    }

    @Test
    void privateRead() throws Exception {
        mvc.perform(get("/public/articles/11")).andExpect(status().isNotFound());
        mvc.perform(get("/public/articles/11").header("Authorization", token("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void guestWrite() throws Exception {
        mvc.perform(post("/my/article/10/like")).andExpect(status().isUnauthorized());
        mvc.perform(
                        post("/my/article/10/comment")
                                .contentType("application/json")
                                .content("{\"content\":\"hi\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void likes() throws Exception {
        for (int i = 0; i < 2; i++)
            mvc.perform(post("/my/article/10/like").header("Authorization", token("reader01")))
                    .andExpect(status().isOk());
        mvc.perform(get("/public/articles/10")).andExpect(jsonPath("$.data.likeCount").value(1));
        for (int i = 0; i < 2; i++)
            mvc.perform(delete("/my/article/10/like").header("Authorization", token("reader01")))
                    .andExpect(status().isOk());
        mvc.perform(get("/public/articles/10")).andExpect(jsonPath("$.data.likeCount").value(0));
    }

    @Test
    void hideFavorite() throws Exception {
        mvc.perform(post("/my/article/10/favorite").header("Authorization", token("reader01")))
                .andExpect(status().isOk());
        var update = new com.silver.diary.entity.Article();
        update.setId(10);
        update.setState("私有");
        articles.updateById(update);
        mvc.perform(get("/my/article/favorites").header("Authorization", token("reader01")))
                .andExpect(jsonPath("$.data.total").value(0));
        mvc.perform(post("/my/article/10/like").header("Authorization", token("reader01")))
                .andExpect(status().isNotFound());
        update.setState("公开");
        articles.updateById(update);
        mvc.perform(get("/my/article/favorites").header("Authorization", token("reader01")))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void comments() throws Exception {
        mvc.perform(
                        post("/my/article/10/comment")
                                .header("Authorization", token("reader01"))
                                .contentType("application/json")
                                .content("{\"content\":\"喜欢这篇日记\"}"))
                .andExpect(status().isOk());
        Integer id = jdbc.queryForObject("SELECT id FROM article_comment", Integer.class);
        mvc.perform(delete("/my/article/comments/" + id).header("Authorization", token("owner01")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/my/article/comments/" + id).header("Authorization", token("reader01")))
                .andExpect(status().isOk());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM article_comment", Integer.class));
    }

    @Test
    void commentLikes() throws Exception {
        jdbc.update(
                "INSERT INTO article_comment (id, article_id, user_id, content, create_time) VALUES (5, 10, 2, 'hi', CURRENT_TIMESTAMP)");
        for (int i = 0; i < 2; i++)
            mvc.perform(
                            post("/my/article/comments/5/like")
                                    .header("Authorization", token("owner01")))
                    .andExpect(status().isOk());
        mvc.perform(get("/public/articles/10/comments"))
                .andExpect(jsonPath("$.data.list[0].likeCount").value(1));
        mvc.perform(delete("/my/article/comments/5").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk());
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM article_comment_like", Integer.class));
    }

    @Test
    void privateComment() throws Exception {
        jdbc.update(
                "INSERT INTO article_comment (id, article_id, user_id, content, create_time) VALUES (5, 11, 2, 'hi', CURRENT_TIMESTAMP)");
        mvc.perform(get("/public/articles/11/comments")).andExpect(status().isNotFound());
        mvc.perform(delete("/my/article/comments/5").header("Authorization", token("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminDelete() throws Exception {
        mvc.perform(post("/my/article/10/favorite").header("Authorization", token("reader01")))
                .andExpect(status().isOk());
        mvc.perform(delete("/admin/diary/articles/10").header("Authorization", token("reader01")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/admin/diary/articles/10").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk());
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM article_favorite", Integer.class));
        mvc.perform(delete("/admin/diary/articles/11").header("Authorization", token("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void privateCover() throws Exception {
        String name = "12345678-1234-1234-1234-123456789012.png";
        jdbc.update("UPDATE article SET cover_img = ? WHERE id = 11", "/uploads/covers/" + name);
        mvc.perform(get("/uploads/covers/" + name)).andExpect(status().isNotFound());
        mvc.perform(get("/uploads/covers/" + name).header("Authorization", token("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void badComment() throws Exception {
        mvc.perform(
                        post("/my/article/10/comment")
                                .header("Authorization", token("reader01"))
                                .contentType("application/json")
                                .content("{\"content\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectOwner() throws Exception {
        mvc.perform(delete("/admin/users/1").header("Authorization", token("ADMIN")))
                .andExpect(status().isForbidden());
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM `user` WHERE id = 1", Integer.class));
    }
}
