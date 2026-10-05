package com.silver.diary.upload;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorage {
    private final Path root;
    private final String prefix;

    public LocalFileStorage(
            @Value("${file.upload-dir}") String directory,
            @Value("${file.access-url-prefix}") String prefix) {
        this.root = Path.of(directory).toAbsolutePath().normalize();
        this.prefix = prefix.replaceAll("/+$", "") + "/";
    }

    public String save(MultipartFile file, String folder) throws IOException {
        if (!java.util.Set.of("avatars", "covers").contains(folder))
            throw new IllegalArgumentException("非法目录");
        var content = UploadValidator.validate(file, false);
        Path directory = root.resolve(folder);
        Files.createDirectories(directory);
        String name = UUID.randomUUID() + content.extension();
        Path target = directory.resolve(name);
        try {
            Files.write(target, content.bytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException cleanup) {
                ex.addSuppressed(cleanup);
            }
            throw ex;
        }
        return prefix + folder + "/" + name;
    }

    public void deleteQuietly(String url) {
        if (url == null || !url.startsWith(prefix)) return;
        String relative = url.substring(prefix.length());
        if (!relative.matches("(?:avatars|covers)/[0-9a-fA-F-]{32,36}\\.(?:png|jpg|jpeg|webp)"))
            return;
        try {
            Files.deleteIfExists(root.resolve(relative));
        } catch (IOException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("旧文件清理失败: {}", url, ex);
        }
    }
}
