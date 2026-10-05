package com.silver.diary.vo;

import com.silver.diary.entity.ArticleComment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleCommentVo extends ArticleComment {
    private String nickname;
    private String avatar;
    private Long likeCount;
    private boolean liked;
}
