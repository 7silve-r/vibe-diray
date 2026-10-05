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
import com.silver.music.dto.PlaylistAddDto;
import com.silver.music.dto.PlaylistQueryDto;
import com.silver.music.dto.PlaylistUpdateDto;
import com.silver.music.entity.Playlist;
import com.silver.music.entity.UserFavorite;
import com.silver.music.enumeration.FavoriteStatus;
import com.silver.music.mapper.PlaylistMapper;
import com.silver.music.mapper.UserFavoriteMapper;
import com.silver.music.service.MinioService;
import com.silver.music.service.PlaylistService;
import com.silver.music.vo.PlaylistDetailVo;
import com.silver.music.vo.PlaylistVo;
import com.silver.music.vo.SongVo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist>
        implements PlaylistService {

    @Autowired private PlaylistMapper playlistMapper;
    @Autowired private UserFavoriteMapper userFavoriteMapper;
    @Autowired private MinioService minioService;

    @Override
    public Result<PageResult<PlaylistVo>> listPlaylists(PlaylistQueryDto playlistDto) {

        Page<Playlist> page = new Page<>(playlistDto.getPageNum(), playlistDto.getPageSize());
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();

        if (playlistDto.getTitle() != null) {
            queryWrapper.like("title", playlistDto.getTitle());
        }
        if (playlistDto.getStyle() != null) {
            queryWrapper.eq("style", playlistDto.getStyle());
        }

        IPage<Playlist> playlistPage = playlistMapper.selectPage(page, queryWrapper);

        List<PlaylistVo> playlistVoList =
                playlistPage.getRecords().stream()
                        .map(
                                playlist -> {
                                    PlaylistVo playlistVo = new PlaylistVo();
                                    BeanUtils.copyProperties(playlist, playlistVo);
                                    return playlistVo;
                                })
                        .toList();

        return Result.success(new PageResult<>(playlistPage.getTotal(), playlistVoList));
    }

    @Override
    public Result<PageResult<Playlist>> listAdminPlaylists(PlaylistQueryDto playlistDto) {

        Page<Playlist> page = new Page<>(playlistDto.getPageNum(), playlistDto.getPageSize());
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();

        if (playlistDto.getTitle() != null) {
            queryWrapper.like("title", playlistDto.getTitle());
        }
        if (playlistDto.getStyle() != null) {
            queryWrapper.eq("style", playlistDto.getStyle());
        }

        queryWrapper.orderByDesc("id");

        IPage<Playlist> playlistPage = playlistMapper.selectPage(page, queryWrapper);

        return Result.success(new PageResult<>(playlistPage.getTotal(), playlistPage.getRecords()));
    }

    @Override
    public Result<List<PlaylistVo>> getRecommendedPlaylists() {

        Long userId = SecurityUtil.optionalUserId();

        if (userId == null) {
            return Result.success(playlistMapper.getRandomPlaylists(10));
        }

        List<Long> favoritePlaylistIds = userFavoriteMapper.getFavoritePlaylistIdsByUserId(userId);
        if (favoritePlaylistIds.isEmpty()) {
            return Result.success(playlistMapper.getRandomPlaylists(10));
        }

        List<String> favoriteStyles = playlistMapper.getFavoritePlaylistStyles(favoritePlaylistIds);
        List<String> styles =
                favoriteStyles.stream()
                        .filter(style -> style != null && !style.isBlank())
                        .distinct()
                        .toList();

        List<PlaylistVo> recommendedPlaylists =
                styles.isEmpty()
                        ? new ArrayList<>()
                        : new ArrayList<>(
                                playlistMapper.getRecommendedPlaylistsByStyles(
                                        styles, favoritePlaylistIds, 10));

        if (recommendedPlaylists.size() < 10) {
            List<PlaylistVo> randomPlaylists = playlistMapper.getRandomPlaylists(10);
            Set<Long> addedPlaylistIds =
                    recommendedPlaylists.stream()
                            .map(PlaylistVo::getPlaylistId)
                            .collect(Collectors.toSet());

            for (PlaylistVo playlist : randomPlaylists) {
                if (recommendedPlaylists.size() >= 10) break;
                if (!addedPlaylistIds.contains(playlist.getPlaylistId())) {
                    recommendedPlaylists.add(playlist);
                    addedPlaylistIds.add(playlist.getPlaylistId());
                }
            }
        }

        return Result.success(recommendedPlaylists);
    }

    @Override
    public Result<PlaylistDetailVo> getPlaylistDetail(Long playlistId) {
        PlaylistDetailVo playlistDetailVo = playlistMapper.getPlaylistDetailById(playlistId);
        if (playlistDetailVo == null) throw new BusinessException(404, "歌单不存在");

        List<SongVo> songVoList = playlistDetailVo.getSongs();
        songVoList.forEach(songVo -> songVo.setFavoriteStatus(FavoriteStatus.NONE.getId()));
        playlistDetailVo.setFavoriteStatus(FavoriteStatus.NONE.getId());

        Long userId = SecurityUtil.optionalUserId();

        if (userId != null) {

            UserFavorite favoritePlaylist =
                    userFavoriteMapper.selectOne(
                            new QueryWrapper<UserFavorite>()
                                    .eq("user_id", userId)
                                    .eq("type", 1)
                                    .eq("playlist_id", playlistId));
            if (favoritePlaylist != null) {
                playlistDetailVo.setFavoriteStatus(FavoriteStatus.SAVED.getId());
            }

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

        return Result.success(playlistDetailVo);
    }

    @Override
    public Result<Long> countPlaylists(String style) {
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        if (style != null) {
            queryWrapper.eq("style", style);
        }

        return Result.success(playlistMapper.selectCount(queryWrapper));
    }

    @Override
    @Transactional
    public Result<Void> addPlaylist(PlaylistAddDto playlistAddDto) {
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", playlistAddDto.getTitle());
        if (playlistMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(MessageConstant.PLAYLIST + MessageConstant.ALREADY_EXISTS);
        }

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(playlistAddDto, playlist);
        playlistMapper.insert(playlist);

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> updatePlaylist(PlaylistUpdateDto playlistUpdateDto) {
        Long playlistId = playlistUpdateDto.getPlaylistId();

        Playlist playlistByTitle =
                playlistMapper.selectOne(
                        new QueryWrapper<Playlist>().eq("title", playlistUpdateDto.getTitle()));
        if (playlistByTitle != null && !playlistByTitle.getPlaylistId().equals(playlistId)) {
            throw new BusinessException(MessageConstant.PLAYLIST + MessageConstant.ALREADY_EXISTS);
        }

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(playlistUpdateDto, playlist);
        if (playlistMapper.updateById(playlist) == 0) {
            throw new BusinessException(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> updatePlaylistCover(Long playlistId, String coverUrl) {
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) throw new BusinessException(404, "资源不存在");
        String cover = playlist.getCoverUrl();

        playlist.setCoverUrl(coverUrl);
        if (playlistMapper.updateById(playlist) == 0) {
            throw new BusinessException(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        com.silver.music.upload.UploadCleanup.afterCommit(minioService, cover);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> deletePlaylist(Long playlistId) {

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            throw new BusinessException(MessageConstant.PLAYLIST + MessageConstant.NOT_FOUND);
        }
        String coverUrl = playlist.getCoverUrl();

        if (coverUrl != null && !coverUrl.isEmpty()) {
            com.silver.music.upload.UploadCleanup.afterCommit(minioService, coverUrl);
        }

        if (playlistMapper.deleteById(playlistId) == 0) {
            throw new BusinessException(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> deletePlaylists(List<Long> playlistIds) {
        List<Playlist> playlists = playlistMapper.selectBatchIds(playlistIds);
        List<String> coverUrlList =
                playlists.stream()
                        .map(Playlist::getCoverUrl)
                        .filter(coverUrl -> coverUrl != null && !coverUrl.isEmpty())
                        .toList();

        for (String coverUrl : coverUrlList) {
            com.silver.music.upload.UploadCleanup.afterCommit(minioService, coverUrl);
        }

        if (playlistMapper.deleteBatchIds(playlistIds) == 0) {
            throw new BusinessException(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS, null);
    }
}
