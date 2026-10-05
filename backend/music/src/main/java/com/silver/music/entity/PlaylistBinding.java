package com.silver.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("tb_playlist_binding")
public class PlaylistBinding implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId(value = "playlist_id", type = IdType.AUTO)
    private Long playlistId;

    @TableField("song_id")
    private Long songId;
}
