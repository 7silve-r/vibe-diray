package com.silver.music.upload;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.silver.music.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class MediaUploadTest {
    @Test
    void rollback() {
        var storage = mock(MinioService.class);
        when(storage.uploadFile(null, "users")).thenReturn("new-url");
        var manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        var service = new MediaUploadService(storage, manager);
        assertThrows(
                IllegalStateException.class,
                () ->
                        service.upload(
                                null,
                                "users",
                                url -> {
                                    throw new IllegalStateException("database");
                                }));
        verify(storage).deleteFileQuietly("new-url");
        verify(manager).rollback(any());
    }

    @Test
    void commitFail() {
        var storage = mock(MinioService.class);
        when(storage.uploadFile(null, "users")).thenReturn("new-url");
        var manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        doThrow(new IllegalStateException("commit")).when(manager).commit(any());
        assertThrows(
                IllegalStateException.class,
                () -> new MediaUploadService(storage, manager).upload(null, "users", url -> {}));
        verify(storage).deleteFileQuietly("new-url");
    }

    @Test
    void cleanup() {
        var storage = mock(MinioService.class);
        TransactionSynchronizationManager.initSynchronization();
        try {
            UploadCleanup.afterCommit(storage, "old-url");
            verifyNoInteractions(storage);
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
            verify(storage).deleteFileQuietly("old-url");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
