package com.silver.music.enumeration;

import lombok.Getter;

@Getter
public enum FavoriteStatus {
    NONE(0, "未收藏"),
    SAVED(1, "已收藏");

    private final Integer id;
    private final String favoriteStatus;

    FavoriteStatus(Integer id, String favoriteStatus) {
        this.id = id;
        this.favoriteStatus = favoriteStatus;
    }
}
