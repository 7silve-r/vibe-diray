package com.silver.diary.controller;

import com.silver.diary.entity.Article;
import com.silver.diary.exception.BusinessException;
import com.silver.diary.service.ArticleService;
import com.silver.diary.utils.SecurityUtil;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class FileController {
    @Value("${file.upload-dir}")
    private String directory;

    @Value("${file.access-url-prefix}")
    private String prefix;

    @Autowired private ArticleService articleService;

    @GetMapping("${file.access-url-prefix}{folder}/{name}")
    public ResponseEntity<Resource> file(@PathVariable String folder, @PathVariable String name)
            throws java.io.IOException {
        if (!java.util.Set.of("avatars", "covers").contains(folder)
                || !name.matches("[0-9a-fA-F-]{36}\\.(png|jpg|jpeg|webp)"))
            throw new BusinessException(404, "文件不存在");
        if ("covers".equals(folder)) {
            Article article =
                    articleService
                            .lambdaQuery()
                            .eq(Article::getCoverImg, prefix + folder + "/" + name)
                            .one();
            Integer uid = null;
            try {
                uid = SecurityUtil.userId();
            } catch (BusinessException ignored) {
            }
            if (article == null
                    || (!"公开".equals(article.getState()) && !article.getCreateUser().equals(uid))) {
                throw new BusinessException(404, "文件不存在");
            }
        }
        Path path = Path.of(directory).toAbsolutePath().normalize().resolve(folder).resolve(name);
        if (!Files.isRegularFile(path)) throw new BusinessException(404, "文件不存在");
        String type = Files.probeContentType(path);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(
                        type == null
                                ? MediaType.APPLICATION_OCTET_STREAM
                                : MediaType.parseMediaType(type))
                .body(new FileSystemResource(path));
    }
}
