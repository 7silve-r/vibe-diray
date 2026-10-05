package com.silver.music.vo;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class PlaylistDetailVo implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long playlistId;

    private String title;

    private String coverUrl;

    private String introduction;

    private List<SongVo> songs;

    private Integer favoriteStatus = 0;

    private List<CommentVo> comments;
}
