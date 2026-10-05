package com.silver.diary.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ArticleDto {
    private String title;
    private Integer cateId;
    private String content;
    private String state;
    private MultipartFile file;
}
