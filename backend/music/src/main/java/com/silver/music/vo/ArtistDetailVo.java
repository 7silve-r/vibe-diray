package com.silver.music.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ArtistDetailVo implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

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
