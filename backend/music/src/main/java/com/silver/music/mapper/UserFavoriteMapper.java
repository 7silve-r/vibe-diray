package com.silver.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silver.music.entity.UserFavorite;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    @Select(
            "SELECT song_id FROM tb_user_favorite WHERE user_id = #{userId} AND type = 0 ORDER BY create_time DESC")
    List<Long> getUserFavoriteSongIds(@Param("userId") Long userId);

    @Select(
            "SELECT playlist_id FROM tb_user_favorite WHERE user_id = #{userId} AND type = 1 ORDER BY create_time DESC")
    List<Long> getUserFavoritePlaylistIds(@Param("userId") Long userId);

    @Select("SELECT song_id FROM tb_user_favorite WHERE user_id = #{userId} AND type = 0")
    List<Long> getFavoriteSongIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT playlist_id FROM tb_user_favorite WHERE user_id = #{userId} AND type = 1")
    List<Long> getFavoritePlaylistIdsByUserId(@Param("userId") Long userId);
}
