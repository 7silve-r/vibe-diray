package com.silver.music.service;

import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    String uploadFile(MultipartFile file, String folder);

    void deleteFile(String fileUrl);

    default void deleteFileQuietly(String url) {
        try {
            deleteFile(url);
        } catch (RuntimeException ex) {
            LoggerFactory.getLogger(MinioService.class).warn("文件清理失败，需后续清理", ex);
        }
    }
}
