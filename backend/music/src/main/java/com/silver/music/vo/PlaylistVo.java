package com.silver.music.vo;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class PlaylistVo implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long playlistId;

    private String title;

    private String coverUrl;
}
