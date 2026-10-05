package com.silver.music.service.impl;

import com.silver.music.service.MinioService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (!java.util.Set.of("users", "artists", "songCovers", "songs", "playlists", "banners")
                .contains(folder)) throw new IllegalArgumentException("Unsupported storage folder");
        try {
            var content =
                    com.silver.music.upload.UploadValidator.validate(file, folder.equals("songs"));
            String fileName = folder + "/" + UUID.randomUUID() + content.extension();
            try (InputStream stream = new java.io.ByteArrayInputStream(content.bytes())) {
                minioClient.putObject(
                        PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                        stream, content.bytes().length, -1)
                                .contentType(content.type())
                                .build());
            }
            return endpoint.replaceAll("/+$", "") + "/" + bucketName + "/" + fileName;
        } catch (com.silver.diary.exception.BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("文件上传失败", ex);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {

            String prefix = endpoint.replaceAll("/+$", "") + "/" + bucketName + "/";
            if (fileUrl == null || !fileUrl.startsWith(prefix)) return;
            String filePath = fileUrl.substring(prefix.length());

            if (!filePath.matches(
                    "(?:users|artists|songCovers|songs|playlists|banners)/[0-9a-fA-F-]{36}\\.(?:png|jpg|mp3|wav|ogg|flac|m4a)"))
                return;

            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build());

        } catch (Exception e) {
            throw new IllegalStateException("文件删除失败", e);
        }
    }
}
