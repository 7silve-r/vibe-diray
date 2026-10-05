package com.silver.music.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommentSongDto implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long songId;

    private String content;
}
