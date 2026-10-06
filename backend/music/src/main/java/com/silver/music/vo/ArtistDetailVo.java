package com.silver.music.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ArtistDetailVo {

    private Long artistId;

    private String artistName;

    private Integer gender;

    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    private String area;

    private String introduction;

    private List<SongVo> songs;
}
