package com.silver.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class PlaylistAddDto implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 100)
    private String title;

    private String introduction;

    private String style;
}
