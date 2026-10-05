package com.silver.music.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ArtistAddDto implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 100)
    private String artistName;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    private String area;

    private String introduction;
}
