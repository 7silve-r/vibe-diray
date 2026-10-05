package com.silver.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("tb_genre")
public class Genre implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId(value = "song_id", type = IdType.AUTO)
    private Long songId;

    @TableField("style_id")
    private Long styleId;
}
