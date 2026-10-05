package com.silver.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.diary.exception.BusinessException;
import com.silver.diary.utils.SecurityUtil;
import com.silver.music.constant.MessageConstant;
import com.silver.music.dto.AdminSongQueryDto;
import com.silver.music.dto.SongAddDto;
import com.silver.music.dto.SongQueryDto;
import com.silver.music.dto.SongUpdateDto;
import com.silver.music.entity.Genre;
import com.silver.music.entity.Song;
import com.silver.music.entity.Style;
import com.silver.music.entity.UserFavorite;
import com.silver.music.enumeration.FavoriteStatus;
import com.silver.music.mapper.GenreMapper;
import com.silver.music.mapper.SongMapper;
import com.silver.music.mapper.StyleMapper;
import com.silver.music.mapper.UserFavoriteMapper;
import com.silver.music.service.MinioService;
import com.silver.music.service.SongService;
import com.silver.music.vo.SongAdminVo;
import com.silver.music.vo.SongDetailVo;
import com.silver.music.vo.SongVo;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    @Autowired private SongMapper songMapper;
    @Autowired private UserFavoriteMapper userFavoriteMapper;
    @Autowired private StyleMapper styleMapper;
    @Autowired private GenreMapper genreMapper;
    @Autowired private MinioService minioService;

    @Override
    public Result<PageResult<SongVo>> listSongs(SongQueryDto songDto) {

        Long userId = SecurityUtil.optionalUserId();

        Page<SongVo> page = new Page<>(songDto.getPageNum(), songDto.getPageSize());
        IPage<SongVo> songPage =
                songMapper.getSongsWithArtist(
                        page, songDto.getSongName(), songDto.getArtistName(), songDto.getAlbum());

        List<SongVo> songVoList =
                songPage.getRecords().stream()
                        .peek(songVo -> songVo.setFavoriteStatus(FavoriteStatus.NONE.getId()))
                        .toList();

        if (userId != null) {

            List<UserFavorite> favoriteSongs =
                    userFavoriteMapper.selectList(
                            new QueryWrapper<UserFavorite>().eq("user_id", userId).eq("type", 0));

            Set<Long> favoriteSongIds =
                    favoriteSongs.stream().map(UserFavorite::getSongId).collect(Collectors.toSet());

            for (SongVo songVo : songVoList) {
                if (favoriteSongIds.contains(songVo.getSongId())) {
                    songVo.setFavoriteStatus(FavoriteStatus.SAVED.getId());
                }
            }
        }

        return Result.success(new PageResult<>(songPage.getTotal(), songVoList));
    }

    @Override
    public Result<PageResult<SongAdminVo>> listAdminSongs(AdminSongQueryDto songDto) {

        Page<SongAdminVo> page = new Page<>(songDto.getPageNum(), songDto.getPageSize());
        IPage<SongAdminVo> songPage =
                songMapper.getSongsWithArtistName(
                        page, songDto.getArtistId(), songDto.getSongName(), songDto.getAlbum());

        return Result.success(new PageResult<>(songPage.getTotal(), songPage.getRecords()));
    }

    @Override
    public Result<List<SongVo>> getRecommendedSongs() {

        Long userId = SecurityUtil.optionalUserId();

        if (userId == null) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        List<Long> favoriteSongIds = userFavoriteMapper.getFavoriteSongIdsByUserId(userId);
        if (favoriteSongIds.isEmpty()) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        List<Long> favoriteStyleIds = songMapper.getFavoriteSongStyles(favoriteSongIds);
        Map<Long, Long> styleFrequency =
                favoriteStyleIds.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<Long> sortedStyleIds =
                styleFrequency.entrySet().stream()
                        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        List<SongVo> candidateSongs =
                sortedStyleIds.isEmpty()
                        ? new ArrayList<>()
                        : new ArrayList<>(
                                songMapper.getRecommendedSongsByStyles(
                                        sortedStyleIds, favoriteSongIds, 80));

        Collections.shuffle(candidateSongs);
        List<SongVo> recommendedSongs =
                candidateSongs.subList(0, Math.min(20, candidateSongs.size()));

        if (recommendedSongs.size() < 20) {
            List<SongVo> randomSongs = songMapper.getRandomSongsWithArtist();
            Set<Long> addedSongIds =
                    recommendedSongs.stream().map(SongVo::getSongId).collect(Collectors.toSet());
            for (SongVo song : randomSongs) {
                if (recommendedSongs.size() >= 20) break;
                if (!addedSongIds.contains(song.getSongId())) {
                    recommendedSongs.add(song);
                    addedSongIds.add(song.getSongId());
                }
            }
        }

        return Result.success(recommendedSongs);
    }

    @Override
    public Result<SongDetailVo> getSongDetail(Long songId) {
        SongDetailVo songDetailVo = songMapper.getSongDetailById(songId);
        if (songDetailVo == null) throw new BusinessException(404, "歌曲不存在");
        songDetailVo.setFavoriteStatus(FavoriteStatus.NONE.getId());

        Long userId = SecurityUtil.optionalUserId();

        if (userId != null) {

            UserFavorite favoriteSong =
                    userFavoriteMapper.selectOne(
                            new QueryWrapper<UserFavorite>()
                                    .eq("user_id", userId)
                                    .eq("type", 0)
                                    .eq("song_id", songId));
            if (favoriteSong != null) {
                songDetailVo.setFavoriteStatus(FavoriteStatus.SAVED.getId());
            }
        }

        return Result.success(songDetailVo);
    }

    @Override
    public Result<Long> countSongs(String style) {
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        if (style != null) {
            queryWrapper.like("style", style);
        }

        return Result.success(songMapper.selectCount(queryWrapper));
    }

    @Override
    @Transactional
    public Result<Void> addSong(SongAddDto songAddDto) {
        Song song = new Song();
        BeanUtils.copyProperties(songAddDto, song);

        if (songMapper.insert(song) == 0) {
            throw new BusinessException(MessageConstant.ADD + MessageConstant.FAILED);
        }

        Long songId = song.getSongId();

        String styleStr = songAddDto.getStyle();
        if (styleStr != null && !styleStr.isEmpty()) {
            List<String> styles = Arrays.asList(styleStr.split(","));

            List<Style> styleList =
                    styleMapper.selectList(new QueryWrapper<Style>().in("name", styles));

            for (Style style : styleList) {
                Genre genre = new Genre();
                genre.setSongId(songId);
                genre.setStyleId(style.getStyleId());
                genreMapper.insert(genre);
            }
        }

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> updateSong(SongUpdateDto songUpdateDto) {

        Song songInDB = songMapper.selectById(songUpdateDto.getSongId());
        if (songInDB == null) {
            throw new BusinessException(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }

        Song song = new Song();
        BeanUtils.copyProperties(songUpdateDto, song);
        if (songMapper.updateById(song) == 0) {
            throw new BusinessException(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        Long songId = songUpdateDto.getSongId();

        genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));

        String styleStr = songUpdateDto.getStyle();
        if (styleStr != null && !styleStr.isEmpty()) {
            List<String> styles = Arrays.asList(styleStr.split(","));

            List<Style> styleList =
                    styleMapper.selectList(new QueryWrapper<Style>().in("name", styles));

            for (Style style : styleList) {
                Genre genre = new Genre();
                genre.setSongId(songId);
                genre.setStyleId(style.getStyleId());
                genreMapper.insert(genre);
            }
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> updateSongCover(Long songId, String coverUrl) {
        Song song = songMapper.selectById(songId);
        if (song == null) throw new BusinessException(404, "资源不存在");
        String cover = song.getCoverUrl();

        song.setCoverUrl(coverUrl);
        if (songMapper.updateById(song) == 0) {
            throw new BusinessException(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        com.silver.music.upload.UploadCleanup.afterCommit(minioService, cover);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> updateSongAudio(Long songId, String audioUrl, String duration) {
        Song song = songMapper.selectById(songId);
        if (song == null) throw new BusinessException(404, "资源不存在");
        String audio = song.getAudioUrl();

        song.setAudioUrl(audioUrl);
        song.setDuration(duration);
        if (songMapper.updateById(song) == 0) {
            throw new BusinessException(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        com.silver.music.upload.UploadCleanup.afterCommit(minioService, audio);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> deleteSong(Long songId) {
        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }
        String cover = song.getCoverUrl();
        String audio = song.getAudioUrl();

        if (cover != null && !cover.isEmpty()) {
            com.silver.music.upload.UploadCleanup.afterCommit(minioService, cover);
        }
        if (audio != null && !audio.isEmpty()) {
            com.silver.music.upload.UploadCleanup.afterCommit(minioService, audio);
        }

        if (songMapper.deleteById(songId) == 0) {
            throw new BusinessException(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> deleteSongs(List<Long> songIds) {

        List<Song> songs = songMapper.selectByIds(songIds);
        List<String> coverUrlList =
                songs.stream()
                        .map(Song::getCoverUrl)
                        .filter(coverUrl -> coverUrl != null && !coverUrl.isEmpty())
                        .toList();
        List<String> audioUrlList =
                songs.stream()
                        .map(Song::getAudioUrl)
                        .filter(audioUrl -> audioUrl != null && !audioUrl.isEmpty())
                        .toList();

        for (String coverUrl : coverUrlList) {
            com.silver.music.upload.UploadCleanup.afterCommit(minioService, coverUrl);
        }
        for (String audioUrl : audioUrlList) {
            com.silver.music.upload.UploadCleanup.afterCommit(minioService, audioUrl);
        }

        if (songMapper.deleteByIds(songIds) == 0) {
            throw new BusinessException(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS, null);
    }
}
