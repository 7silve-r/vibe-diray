package com.silver.music.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CommentVo implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long commentId;

    private String username;

    private String userAvatar;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;

    private Long likeCount;
}
