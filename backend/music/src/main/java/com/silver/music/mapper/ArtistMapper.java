package com.silver.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silver.music.entity.Artist;
import com.silver.music.vo.ArtistDetailVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArtistMapper extends BaseMapper<Artist> {

    ArtistDetailVo getArtistDetailById(Long artistId);
}
