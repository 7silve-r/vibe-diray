package com.silver.music.dto;

import com.silver.music.enumeration.BannerStatusEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class BannerQueryDto implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @NotNull
    @Min(1)
    private Integer pageNum = 1;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    private BannerStatusEnum bannerStatus;
}
