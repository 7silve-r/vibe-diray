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
import com.silver.music.dto.PlaylistQueryDto;
import com.silver.music.dto.SongQueryDto;
import com.silver.music.entity.UserFavorite;
import com.silver.music.enumeration.FavoriteStatus;
import com.silver.music.mapper.PlaylistMapper;
import com.silver.music.mapper.SongMapper;
import com.silver.music.mapper.UserFavoriteMapper;
import com.silver.music.service.UserFavoriteService;
import com.silver.music.vo.PlaylistVo;
import com.silver.music.vo.SongVo;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements UserFavoriteService {

    @Autowired private UserFavoriteMapper userFavoriteMapper;
    @Autowired private SongMapper songMapper;
    @Autowired private PlaylistMapper playlistMapper;

    @Override
    public Result<PageResult<SongVo>> getUserFavoriteSongs(SongQueryDto songDto) {
        Long userId = SecurityUtil.userId().longValue();

        List<Long> favoriteSongIds = userFavoriteMapper.getUserFavoriteSongIds(userId);
        if (favoriteSongIds.isEmpty()) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        Page<SongVo> page = new Page<>(songDto.getPageNum(), songDto.getPageSize());
        IPage<SongVo> songPage =
                songMapper.getSongsByIds(
                        userId,
                        page,
                        favoriteSongIds,
                        songDto.getSongName(),
                        songDto.getArtistName(),
                        songDto.getAlbum());

        List<SongVo> songVoList =
                songPage.getRecords().stream()
                        .peek(songVo -> songVo.setFavoriteStatus(FavoriteStatus.SAVED.getId()))
                        .toList();

        return Result.success(new PageResult<>(songPage.getTotal(), songVoList));
    }

    @Override
    @Transactional
    public Result<Void> collectSong(Long songId) {
        if (songMapper.lock(songId) == null) throw new BusinessException(404, "歌曲不存在");
        Long userId = SecurityUtil.userId().longValue();

        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("type", 0).eq("song_id", songId);
        if (userFavoriteMapper.selectCount(queryWrapper) > 0) {
            return Result.success();
        }

        UserFavorite userFavorite = new UserFavorite();
        userFavorite.setUserId(userId);
        userFavorite.setType(0);
        userFavorite.setSongId(songId);
        userFavorite.setCreateTime(LocalDateTime.now());
        userFavoriteMapper.insert(userFavorite);

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS, null);
    }

    @Override
    public Result<Void> cancelCollectSong(Long songId) {
        Long userId = SecurityUtil.userId().longValue();

        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("type", 0).eq("song_id", songId);
        userFavoriteMapper.delete(queryWrapper);

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS, null);
    }

    @Override
    public Result<PageResult<PlaylistVo>> getUserFavoritePlaylists(PlaylistQueryDto playlistDto) {
        Long userId = SecurityUtil.userId().longValue();

        List<Long> favoritePlaylistIds = userFavoriteMapper.getUserFavoritePlaylistIds(userId);
        if (favoritePlaylistIds.isEmpty()) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        Page<PlaylistVo> page = new Page<>(playlistDto.getPageNum(), playlistDto.getPageSize());
        IPage<PlaylistVo> playlistPage =
                playlistMapper.getPlaylistsByIds(
                        userId,
                        page,
                        favoritePlaylistIds,
                        playlistDto.getTitle(),
                        playlistDto.getStyle());

        return Result.success(new PageResult<>(playlistPage.getTotal(), playlistPage.getRecords()));
    }

    @Override
    @Transactional
    public Result<Void> collectPlaylist(Long playlistId) {
        if (playlistMapper.lock(playlistId) == null) throw new BusinessException(404, "歌单不存在");
        Long userId = SecurityUtil.userId().longValue();

        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("type", 1).eq("playlist_id", playlistId);
        if (userFavoriteMapper.selectCount(queryWrapper) > 0) {
            return Result.success();
        }

        UserFavorite userFavorite = new UserFavorite();
        userFavorite.setUserId(userId);
        userFavorite.setType(1);
        userFavorite.setPlaylistId(playlistId);
        userFavorite.setCreateTime(LocalDateTime.now());
        userFavoriteMapper.insert(userFavorite);

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS, null);
    }

    @Override
    public Result<Void> cancelCollectPlaylist(Long playlistId) {
        Long userId = SecurityUtil.userId().longValue();

        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("type", 1).eq("playlist_id", playlistId);
        userFavoriteMapper.delete(queryWrapper);

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS, null);
    }
}
