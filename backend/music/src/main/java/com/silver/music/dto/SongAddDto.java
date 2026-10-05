package com.silver.music.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
public class SongAddDto implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @NotNull @Positive private Long artistId;

    @NotBlank
    @Size(max = 100)
    private String songName;

    @NotBlank
    @Size(max = 100)
    private String album;

    private String style;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate releaseTime;
}
