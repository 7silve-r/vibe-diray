package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.ArtistAddDto;
import com.silver.music.dto.ArtistQueryDto;
import com.silver.music.dto.ArtistUpdateDto;
import com.silver.music.entity.Artist;
import com.silver.music.vo.ArtistDetailVo;
import com.silver.music.vo.ArtistNameVo;
import com.silver.music.vo.ArtistVo;
import java.util.List;

public interface ArtistService extends IService<Artist> {

    Result<PageResult<ArtistVo>> listArtists(ArtistQueryDto artistDto);

    Result<PageResult<Artist>> listAdminArtists(ArtistQueryDto artistDto);

    Result<List<ArtistNameVo>> listArtistNames();

    Result<List<ArtistVo>> getRandomArtists();

    Result<ArtistDetailVo> getArtistDetail(Long artistId);

    Result<Long> countArtists(Integer gender, String area);

    Result<Void> addArtist(ArtistAddDto artistAddDto);

    Result<Void> updateArtist(ArtistUpdateDto artistUpdateDto);

    Result<Void> updateArtistAvatar(Long artistId, String avatar);

    Result<Void> deleteArtist(Long ArtistId);

    Result<Void> deleteArtists(List<Long> artistIds);
}
