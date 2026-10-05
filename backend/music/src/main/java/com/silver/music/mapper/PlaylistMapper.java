package com.silver.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silver.music.entity.Playlist;
import com.silver.music.vo.PlaylistDetailVo;
import com.silver.music.vo.PlaylistVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {
    @Select("SELECT * FROM tb_playlist WHERE id = #{id} FOR UPDATE")
    Playlist lock(Long id);

    PlaylistDetailVo getPlaylistDetailById(Long playlistId);

    List<String> getFavoritePlaylistStyles(List<Long> favoritePlaylistIds);

    List<PlaylistVo> getRecommendedPlaylistsByStyles(
            List<String> styles, List<Long> favoritePlaylistIds, int limit);

    @Select(
            """
            SELECT
                p.id AS playlistId,
                p.title AS title,
                p.cover_url AS coverUrl
            FROM tb_playlist p
            ORDER BY RAND()
            LIMIT #{limit}
            """)
    List<PlaylistVo> getRandomPlaylists(int limit);

    IPage<PlaylistVo> getPlaylistsByIds(
            Long userId,
            Page<PlaylistVo> page,
            @Param("playlistIds") List<Long> playlistIds,
            @Param("title") String title,
            @Param("style") String style);
}
