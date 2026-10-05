package com.silver.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silver.music.entity.Genre;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GenreMapper extends BaseMapper<Genre> {}
