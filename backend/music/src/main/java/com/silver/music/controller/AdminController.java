package com.silver.music.controller;

import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.*;
import com.silver.music.entity.Artist;
import com.silver.music.entity.Playlist;
import com.silver.music.service.*;
import com.silver.music.vo.ArtistNameVo;
import com.silver.music.vo.SongAdminVo;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/music/admin")
public class AdminController {

    @Autowired private ArtistService artistService;
    @Autowired private SongService songService;
    @Autowired private PlaylistService playlistService;

    @GetMapping("/countArtists")
    public Result<Long> countArtists(
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) String area) {
        return artistService.countArtists(gender, area);
    }

    @PostMapping("/listArtists")
    public Result<PageResult<Artist>> listArtists(@RequestBody @Valid ArtistQueryDto artistDto) {
        return artistService.listAdminArtists(artistDto);
    }

    @PostMapping("/addArtist")
    public Result<Void> addArtist(@RequestBody @Valid ArtistAddDto artistAddDto) {
        return artistService.addArtist(artistAddDto);
    }

    @PutMapping("/updateArtist")
    public Result<Void> updateArtist(@RequestBody @Valid ArtistUpdateDto artistUpdateDto) {
        return artistService.updateArtist(artistUpdateDto);
    }

    @DeleteMapping("/deleteArtist/{id}")
    public Result<Void> deleteArtist(@PathVariable("id") Long artistId) {
        return artistService.deleteArtist(artistId);
    }

    @DeleteMapping("/deleteArtists")
    public Result<Void> deleteArtists(@RequestBody List<Long> artistIds) {
        return artistService.deleteArtists(artistIds);
    }

    @GetMapping("/countSongs")
    public Result<Long> countSongs(@RequestParam(required = false) String style) {
        return songService.countSongs(style);
    }

    @GetMapping("/listArtistNames")
    public Result<List<ArtistNameVo>> listArtistNames() {
        return artistService.listArtistNames();
    }

    @PostMapping("/listAdminSongs")
    public Result<PageResult<SongAdminVo>> listAdminSongs(
            @RequestBody @Valid AdminSongQueryDto songDto) {
        return songService.listAdminSongs(songDto);
    }

    @PostMapping("/addSong")
    public Result<Void> addSong(@RequestBody @Valid SongAddDto songAddDto) {
        return songService.addSong(songAddDto);
    }

    @PutMapping("/updateSong")
    public Result<Void> updateSong(@RequestBody @Valid SongUpdateDto songUpdateDto) {
        return songService.updateSong(songUpdateDto);
    }

    @DeleteMapping("/deleteSong/{id}")
    public Result<Void> deleteSong(@PathVariable("id") Long songId) {
        return songService.deleteSong(songId);
    }

    @DeleteMapping("/deleteSongs")
    public Result<Void> deleteSongs(@RequestBody List<Long> songIds) {
        return songService.deleteSongs(songIds);
    }

    @GetMapping("/countPlaylists")
    public Result<Long> countPlaylists(@RequestParam(required = false) String style) {
        return playlistService.countPlaylists(style);
    }

    @PostMapping("/listPlaylists")
    public Result<PageResult<Playlist>> listPlaylists(
            @RequestBody @Valid PlaylistQueryDto playlistDto) {
        return playlistService.listAdminPlaylists(playlistDto);
    }

    @PostMapping("/addPlaylist")
    public Result<Void> addPlaylist(@RequestBody @Valid PlaylistAddDto playlistAddDto) {
        return playlistService.addPlaylist(playlistAddDto);
    }

    @PutMapping("/updatePlaylist")
    public Result<Void> updatePlaylist(@RequestBody @Valid PlaylistUpdateDto playlistUpdateDto) {
        return playlistService.updatePlaylist(playlistUpdateDto);
    }

    @DeleteMapping("/deletePlaylist/{id}")
    public Result<Void> deletePlaylist(@PathVariable("id") Long playlistId) {
        return playlistService.deletePlaylist(playlistId);
    }

    @DeleteMapping("/deletePlaylists")
    public Result<Void> deletePlaylists(@RequestBody List<Long> playlistIds) {
        return playlistService.deletePlaylists(playlistIds);
    }
}
