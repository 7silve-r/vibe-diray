package com.silver.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silver.music.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("SELECT *, id AS comment_id FROM tb_comment WHERE id = #{id} FOR UPDATE")
    Comment lock(Long id);
}
