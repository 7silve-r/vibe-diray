package com.silver.music.controller;

import com.silver.diary.common.Result;
import com.silver.music.dto.CommentPlaylistDto;
import com.silver.music.dto.CommentSongDto;
import com.silver.music.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping("/music/comment")
public class CommentController {

    @Autowired private CommentService commentService;

    @PostMapping("/addSongComment")
    public Result<Void> addSongComment(@RequestBody @Valid CommentSongDto commentSongDto) {
        return commentService.addSongComment(commentSongDto);
    }

    @PostMapping("/addPlaylistComment")
    public Result<Void> addPlaylistComment(
            @RequestBody @Valid CommentPlaylistDto commentPlaylistDto) {
        return commentService.addPlaylistComment(commentPlaylistDto);
    }

    @PatchMapping("/likeComment/{id}")
    public Result<Void> likeComment(@PathVariable("id") Long commentId) {
        return commentService.likeComment(commentId);
    }

    @PatchMapping("/cancelLikeComment/{id}")
    public Result<Void> cancelLikeComment(@PathVariable("id") Long commentId) {
        return commentService.cancelLikeComment(commentId);
    }

    @DeleteMapping("/deleteComment/{id}")
    public Result<Void> deleteComment(@PathVariable("id") Long commentId) {
        return commentService.deleteComment(commentId);
    }
}
