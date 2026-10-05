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
import com.silver.music.dto.ArtistAddDto;
import com.silver.music.dto.ArtistQueryDto;
import com.silver.music.dto.ArtistUpdateDto;
import com.silver.music.entity.Artist;
import com.silver.music.entity.UserFavorite;
import com.silver.music.enumeration.FavoriteStatus;
import com.silver.music.mapper.ArtistMapper;
import com.silver.music.mapper.UserFavoriteMapper;
import com.silver.music.service.ArtistService;
import com.silver.music.service.MinioService;
import com.silver.music.vo.ArtistDetailVo;
import com.silver.music.vo.ArtistNameVo;
import com.silver.music.vo.ArtistVo;
import com.silver.music.vo.SongVo;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArtistServiceImpl extends ServiceImpl<ArtistMapper, Artist> implements ArtistService {

    @Autowired private ArtistMapper artistMapper;
    @Autowired private UserFavoriteMapper userFavoriteMapper;
    @Autowired private MinioService minioService;

    @Override
    public Result<PageResult<ArtistVo>> listArtists(ArtistQueryDto artistDto) {

        Page<Artist> page = new Page<>(artistDto.getPageNum(), artistDto.getPageSize());
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();

        if (artistDto.getArtistName() != null) {
            queryWrapper.like("name", artistDto.getArtistName());
        }
        if (artistDto.getGender() != null) {
            queryWrapper.eq("gender", artistDto.getGender());
        }
        if (artistDto.getArea() != null) {
            queryWrapper.like("area", artistDto.getArea());
        }

        IPage<Artist> artistPage = artistMapper.selectPage(page, queryWrapper);

        List<ArtistVo> artistVoList =
                artistPage.getRecords().stream()
                        .map(
                                artist -> {
                                    ArtistVo artistVo = new ArtistVo();
                                    BeanUtils.copyProperties(artist, artistVo);
                                    return artistVo;
                                })
                        .toList();

        return Result.success(new PageResult<>(artistPage.getTotal(), artistVoList));
    }

    @Override
    public Result<PageResult<Artist>> listAdminArtists(ArtistQueryDto artistDto) {

        Page<Artist> page = new Page<>(artistDto.getPageNum(), artistDto.getPageSize());
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();

        if (artistDto.getArtistName() != null) {
            queryWrapper.like("name", artistDto.getArtistName());
        }
        if (artistDto.getGender() != null) {
            queryWrapper.eq("gender", artistDto.getGender());
        }
        if (artistDto.getArea() != null) {
            queryWrapper.like("area", artistDto.getArea());
        }

        queryWrapper.orderByDesc("id");

        IPage<Artist> artistPage = artistMapper.selectPage(page, queryWrapper);

        return Result.success(new PageResult<>(artistPage.getTotal(), artistPage.getRecords()));
    }

    @Override
    public Result<List<ArtistNameVo>> listArtistNames() {
        List<Artist> artists =
                artistMapper.selectList(new QueryWrapper<Artist>().orderByDesc("id"));

        List<ArtistNameVo> artistNameVoList =
                artists.stream()
                        .map(
                                artist -> {
                                    ArtistNameVo artistNameVo = new ArtistNameVo();
                                    artistNameVo.setArtistId(artist.getArtistId());
                                    artistNameVo.setArtistName(artist.getArtistName());
                                    return artistNameVo;
                                })
                        .toList();

        return Result.success(artistNameVoList);
    }

    @Override
    public Result<List<ArtistVo>> getRandomArtists() {
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        queryWrapper.last("ORDER BY RAND() LIMIT 10");

        List<Artist> artists = artistMapper.selectList(queryWrapper);

        List<ArtistVo> artistVoList =
                artists.stream()
                        .map(
                                artist -> {
                                    ArtistVo artistVo = new ArtistVo();
                                    BeanUtils.copyProperties(artist, artistVo);
                                    return artistVo;
                                })
                        .toList();

        return Result.success(artistVoList);
    }

    @Override
    public Result<ArtistDetailVo> getArtistDetail(Long artistId) {
        ArtistDetailVo artistDetailVo = artistMapper.getArtistDetailById(artistId);
        if (artistDetailVo == null) throw new BusinessException(404, "歌手不存在");

        List<SongVo> songVoList = artistDetailVo.getSongs();
        songVoList.forEach(songVo -> songVo.setFavoriteStatus(FavoriteStatus.NONE.getId()));

        Long userId = SecurityUtil.optionalUserId();

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

        artistDetailVo.setSongs(songVoList);

        return Result.success(artistDetailVo);
    }

    @Override
    public Result<Long> countArtists(Integer gender, String area) {
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        if (gender != null) {
            queryWrapper.eq("gender", gender);
        }
        if (area != null) {
            queryWrapper.eq("area", area);
        }

        return Result.success(artistMapper.selectCount(queryWrapper));
    }

    @Override
    @Transactional
    public Result<Void> addArtist(ArtistAddDto artistAddDto) {
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", artistAddDto.getArtistName());
        if (artistMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(MessageConstant.ARTIST + MessageConstant.ALREADY_EXISTS);
        }

        Artist artist = new Artist();
        BeanUtils.copyProperties(artistAddDto, artist);
        artistMapper.insert(artist);

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> updateArtist(ArtistUpdateDto artistUpdateDto) {
        Long artistId = artistUpdateDto.getArtistId();

        Artist artistByArtistName =
                artistMapper.selectOne(
                        new QueryWrapper<Artist>().eq("name", artistUpdateDto.getArtistName()));
        if (artistByArtistName != null && !artistByArtistName.getArtistId().equals(artistId)) {
            throw new BusinessException(MessageConstant.ARTIST + MessageConstant.ALREADY_EXISTS);
        }

        Artist artist = new Artist();
        BeanUtils.copyProperties(artistUpdateDto, artist);
        if (artistMapper.updateById(artist) == 0) {
            throw new BusinessException(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> updateArtistAvatar(Long artistId, String avatar) {
        Artist artist = artistMapper.selectById(artistId);
        if (artist == null) throw new BusinessException(404, "资源不存在");
        String avatarUrl = artist.getAvatar();

        artist.setAvatar(avatar);
        if (artistMapper.updateById(artist) == 0) {
            throw new BusinessException(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        com.silver.music.upload.UploadCleanup.afterCommit(minioService, avatarUrl);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> deleteArtist(Long artistId) {

        Artist artist = artistMapper.selectById(artistId);
        if (artist == null) {
            throw new BusinessException(MessageConstant.ARTIST + MessageConstant.NOT_FOUND);
        }
        String avatarUrl = artist.getAvatar();

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            com.silver.music.upload.UploadCleanup.afterCommit(minioService, avatarUrl);
        }

        if (artistMapper.deleteById(artistId) == 0) {
            throw new BusinessException(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS, null);
    }

    @Override
    @Transactional
    public Result<Void> deleteArtists(List<Long> artistIds) {

        List<Artist> artists = artistMapper.selectByIds(artistIds);
        List<String> avatarUrlList =
                artists.stream()
                        .map(Artist::getAvatar)
                        .filter(avatarUrl -> avatarUrl != null && !avatarUrl.isEmpty())
                        .toList();

        for (String avatarUrl : avatarUrlList) {
            com.silver.music.upload.UploadCleanup.afterCommit(minioService, avatarUrl);
        }

        if (artistMapper.deleteByIds(artistIds) == 0) {
            throw new BusinessException(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS, null);
    }
}
