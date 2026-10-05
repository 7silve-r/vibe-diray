package com.silver.music.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommentPlaylistDto implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long playlistId;

    private String content;
}
