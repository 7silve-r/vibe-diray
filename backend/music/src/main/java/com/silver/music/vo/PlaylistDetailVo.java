package com.silver.music.vo;

import java.util.List;
import lombok.Data;

@Data
public class PlaylistDetailVo {

    private Long playlistId;

    private String title;

    private String coverUrl;

    private String introduction;

    private List<SongVo> songs;

    private Integer favoriteStatus = 0;

    private List<CommentVo> comments;
}
