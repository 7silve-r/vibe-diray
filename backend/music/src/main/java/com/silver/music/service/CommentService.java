package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.diary.common.Result;
import com.silver.music.dto.CommentPlaylistDto;
import com.silver.music.dto.CommentSongDto;
import com.silver.music.entity.Comment;

public interface CommentService extends IService<Comment> {

    Result<Void> addSongComment(CommentSongDto commentSongDto);

    Result<Void> addPlaylistComment(CommentPlaylistDto commentPlaylistDto);

    Result<Void> likeComment(Long commentId);

    Result<Void> cancelLikeComment(Long commentId);

    Result<Void> deleteComment(Long commentId);
}
