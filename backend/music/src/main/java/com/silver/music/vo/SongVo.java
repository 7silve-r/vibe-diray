package com.silver.music.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
public class SongVo implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long songId;

    private String songName;

    private String artistName;

    private String album;

    private String duration;

    private String coverUrl;

    private String audioUrl;

    private Integer favoriteStatus = 0;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;
}
