package com.silver.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("tb_playlist")
public class Playlist implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long playlistId;

    @TableField("title")
    private String title;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("introduction")
    private String introduction;

    @TableField("style")
    private String style;
}
