package com.silver.music.vo;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class ArtistNameVo implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long artistId;

    private String artistName;
}
