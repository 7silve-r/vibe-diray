package com.silver.music.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CommentVo {

    private Long commentId;

    private String username;

    private String userAvatar;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;

    private Long likeCount;
}
