package com.silver.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlaylistUpdateDto {

    @NotNull @Positive private Long playlistId;

    @NotBlank
    @Size(max = 100)
    private String title;

    private String introduction;

    private String style;
}
