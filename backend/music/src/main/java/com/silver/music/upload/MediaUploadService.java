package com.silver.music.upload;

import com.silver.music.service.MinioService;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaUploadService {
    private final MinioService storage;
    private final TransactionTemplate transactions;

    public MediaUploadService(MinioService storage, PlatformTransactionManager manager) {
        this.storage = storage;
        this.transactions = new TransactionTemplate(manager);
    }

    public UploadResult upload(MultipartFile file, String folder, Consumer<String> update) {
        String url = storage.uploadFile(file, folder);
        try {
            transactions.executeWithoutResult(status -> update.accept(url));
            return new UploadResult(url);
        } catch (RuntimeException ex) {
            storage.deleteFileQuietly(url);
            throw ex;
        }
    }
}
