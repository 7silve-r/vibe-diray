package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.music.dto.CommentPlaylistDto;
import com.silver.music.dto.CommentSongDto;
import com.silver.music.entity.Comment;

public interface CommentService extends IService<Comment> {

    void addSongComment(CommentSongDto commentSongDto);

    void addPlaylistComment(CommentPlaylistDto commentPlaylistDto);

    void likeComment(Long commentId);

    void cancelLikeComment(Long commentId);

    void deleteComment(Long commentId);
}
