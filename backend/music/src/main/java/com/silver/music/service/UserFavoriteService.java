package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.PlaylistQueryDto;
import com.silver.music.dto.SongQueryDto;
import com.silver.music.entity.UserFavorite;
import com.silver.music.vo.PlaylistVo;
import com.silver.music.vo.SongVo;

public interface UserFavoriteService extends IService<UserFavorite> {

    Result<PageResult<SongVo>> getUserFavoriteSongs(SongQueryDto songDto);

    Result<Void> collectSong(Long songId);

    Result<Void> cancelCollectSong(Long songId);

    Result<PageResult<PlaylistVo>> getUserFavoritePlaylists(PlaylistQueryDto playlistDto);

    Result<Void> collectPlaylist(Long playlistId);

    Result<Void> cancelCollectPlaylist(Long playlistId);
}
