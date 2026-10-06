package com.silver.music.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum BannerStatus {
    ENABLE(0, "启用"),
    DISABLE(1, "禁用");

    @EnumValue private final Integer id;
    private final String bannerStatus;

    BannerStatus(Integer id, String bannerStatus) {
        this.id = id;
        this.bannerStatus = bannerStatus;
    }
}
