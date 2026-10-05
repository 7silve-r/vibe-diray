package com.silver.music.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class SongDetailVo implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long songId;

    private String songName;

    private String artistName;

    private String album;

    private String lyric;

    private String duration;

    private String coverUrl;

    private String audioUrl;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;

    private Integer favoriteStatus = 0;

    private List<CommentVo> comments;
}
