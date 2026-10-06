package com.silver.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlaylistAddDto {

    @NotBlank
    @Size(max = 100)
    private String title;

    private String introduction;

    private String style;
}
