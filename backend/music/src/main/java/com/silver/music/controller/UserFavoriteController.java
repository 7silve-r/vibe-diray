package com.silver.music.controller;

import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.PlaylistQueryDto;
import com.silver.music.dto.SongQueryDto;
import com.silver.music.service.UserFavoriteService;
import com.silver.music.vo.PlaylistVo;
import com.silver.music.vo.SongVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping("/music/favorite")
public class UserFavoriteController {

    @Autowired private UserFavoriteService userFavoriteService;

    @PostMapping("/getFavoriteSongs")
    public Result<PageResult<SongVo>> getUserFavoriteSongs(
            @RequestBody @Valid SongQueryDto songDto) {
        return userFavoriteService.getUserFavoriteSongs(songDto);
    }

    @PostMapping("/collectSong")
    public Result<Void> collectSong(@RequestParam Long songId) {
        return userFavoriteService.collectSong(songId);
    }

    @DeleteMapping("/cancelCollectSong")
    public Result<Void> cancelCollectSong(@RequestParam Long songId) {
        return userFavoriteService.cancelCollectSong(songId);
    }

    @PostMapping("/getFavoritePlaylists")
    public Result<PageResult<PlaylistVo>> getFavoritePlaylists(
            @RequestBody @Valid PlaylistQueryDto playlistDto) {
        return userFavoriteService.getUserFavoritePlaylists(playlistDto);
    }

    @PostMapping("/collectPlaylist")
    public Result<Void> collectPlaylist(@RequestParam Long playlistId) {
        return userFavoriteService.collectPlaylist(playlistId);
    }

    @DeleteMapping("/cancelCollectPlaylist")
    public Result<Void> cancelCollectPlaylist(@RequestParam Long playlistId) {
        return userFavoriteService.cancelCollectPlaylist(playlistId);
    }
}
