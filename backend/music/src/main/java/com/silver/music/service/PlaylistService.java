package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.PlaylistAddDto;
import com.silver.music.dto.PlaylistQueryDto;
import com.silver.music.dto.PlaylistUpdateDto;
import com.silver.music.entity.Playlist;
import com.silver.music.vo.PlaylistDetailVo;
import com.silver.music.vo.PlaylistVo;
import java.util.List;

public interface PlaylistService extends IService<Playlist> {

    Result<PageResult<PlaylistVo>> listPlaylists(PlaylistQueryDto playlistDto);

    Result<PageResult<Playlist>> listAdminPlaylists(PlaylistQueryDto playlistDto);

    Result<List<PlaylistVo>> getRecommendedPlaylists();

    Result<PlaylistDetailVo> getPlaylistDetail(Long playlistId);

    Result<Long> countPlaylists(String style);

    Result<Void> addPlaylist(PlaylistAddDto playlistAddDto);

    Result<Void> updatePlaylist(PlaylistUpdateDto playlistUpdateDto);

    Result<Void> updatePlaylistCover(Long playlistId, String coverUrl);

    Result<Void> deletePlaylist(Long playlistId);

    Result<Void> deletePlaylists(List<Long> playlistIds);
}
