package com.silver.diary.upload;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.silver.diary.entity.*;
import com.silver.diary.exception.BusinessException;
import com.silver.diary.service.*;
import com.silver.diary.support.TestData;
import java.io.IOException;
import org.junit.jupiter.api.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.SimpleTransactionStatus;

/** 验证事务协调和文件补偿顺序，真实数据库事务另需集成测试。 */
class UploadServiceTest {
    LocalFileStorage storage;
    UserService users;
    ArticleService articles;
    PlatformTransactionManager manager;
    SimpleTransactionStatus status;
    UploadService service;

    @BeforeEach
    void setup() {
        storage = mock(LocalFileStorage.class);
        users = mock(UserService.class);
        articles = mock(ArticleService.class);
        manager = mock(PlatformTransactionManager.class);
        status = new SimpleTransactionStatus();
        when(manager.getTransaction(any())).thenReturn(status);
        service = new UploadService(storage, users, articles, manager);
    }

    Article article() {
        var a = new Article();
        a.setId(7);
        a.setCoverImg("/uploads/covers/old.png");
        return a;
    }

    @Test
    void commitFirst() throws Exception {
        var file = TestData.png();
        when(storage.save(file, "covers")).thenReturn("new.png");
        when(articles.updateById(any(Article.class))).thenReturn(true);
        assertEquals("new.png", service.article(article(), file).url());
        var order = inOrder(articles, manager, storage);
        order.verify(articles).updateById(any(Article.class));
        order.verify(manager).commit(status);
        order.verify(storage).deleteQuietly("/uploads/covers/old.png");
        verify(storage, never()).deleteQuietly("new.png");
    }

    @Test
    void rollback() throws Exception {
        var file = TestData.png();
        when(storage.save(file, "covers")).thenReturn("new.png");
        assertThrows(BusinessException.class, () -> service.article(article(), file));
        verify(manager).rollback(status);
        verify(storage).deleteQuietly("new.png");
        verify(storage, never()).deleteQuietly("/uploads/covers/old.png");
    }

    @Test
    void commitFail() throws Exception {
        var file = TestData.png();
        when(storage.save(file, "covers")).thenReturn("new.png");
        when(articles.updateById(any(Article.class))).thenReturn(true);
        doThrow(new TransactionSystemException("failed commit")).when(manager).commit(status);
        assertThrows(TransactionSystemException.class, () -> service.article(article(), file));
        verify(storage).deleteQuietly("new.png");
        verify(storage, never()).deleteQuietly("/uploads/covers/old.png");
    }

    @Test
    void keepCover() throws Exception {
        when(articles.updateById(any(Article.class))).thenReturn(true);
        assertEquals("/uploads/covers/old.png", service.article(article(), null).url());
        verifyNoInteractions(storage);
    }

    @Test
    void ioFail() throws Exception {
        var file = TestData.png();
        when(storage.save(file, "covers")).thenThrow(new IOException("disk full"));
        assertThrows(IOException.class, () -> service.article(article(), file));
        verifyNoInteractions(articles, manager);
    }

    @Test
    void goneUser() {
        TestData.users(users);
        assertEquals(
                401,
                assertThrows(BusinessException.class, () -> service.avatar("missing", null))
                        .getCode());
        verifyNoInteractions(storage, manager);
    }

    @Test
    void newArticle() throws Exception {
        when(articles.save(any(Article.class))).thenReturn(true);
        service.article(new Article(), null);
        verify(articles).save(any(Article.class));
        verify(articles, never()).updateById(any(Article.class));
        verify(manager).commit(status);
    }
}
