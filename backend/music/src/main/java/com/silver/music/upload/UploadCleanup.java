package com.silver.music.upload;

import com.silver.music.service.MinioService;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class UploadCleanup {
    private UploadCleanup() {}

    public static void afterCommit(MinioService storage, String oldUrl) {
        if (oldUrl == null || oldUrl.isBlank()) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            storage.deleteFileQuietly(oldUrl);
                        }
                    });
        } else {
            storage.deleteFileQuietly(oldUrl);
        }
    }
}
