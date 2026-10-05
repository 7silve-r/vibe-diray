package com.silver.music.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
public class SongAdminVo implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long songId;

    private String artistName;

    private String songName;

    private String album;

    private String lyric;

    private String duration;

    private String style;

    private String coverUrl;

    private String audioUrl;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;
}
