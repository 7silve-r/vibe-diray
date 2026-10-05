package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.AdminSongQueryDto;
import com.silver.music.dto.SongAddDto;
import com.silver.music.dto.SongQueryDto;
import com.silver.music.dto.SongUpdateDto;
import com.silver.music.entity.Song;
import com.silver.music.vo.SongAdminVo;
import com.silver.music.vo.SongDetailVo;
import com.silver.music.vo.SongVo;
import java.util.List;

public interface SongService extends IService<Song> {

    Result<PageResult<SongVo>> listSongs(SongQueryDto songDto);

    Result<PageResult<SongAdminVo>> listAdminSongs(AdminSongQueryDto songDto);

    Result<List<SongVo>> getRecommendedSongs();

    Result<SongDetailVo> getSongDetail(Long songId);

    Result<Long> countSongs(String style);

    Result<Void> addSong(SongAddDto songAddDto);

    Result<Void> updateSong(SongUpdateDto songUpdateDto);

    Result<Void> updateSongCover(Long songId, String coverUrl);

    Result<Void> updateSongAudio(Long songId, String audioUrl, String duration);

    Result<Void> deleteSong(Long songId);

    Result<Void> deleteSongs(List<Long> songIds);
}
