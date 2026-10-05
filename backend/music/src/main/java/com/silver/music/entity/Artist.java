package com.silver.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
@TableName("tb_artist")
public class Artist implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long artistId;

    @TableField("name")
    private String artistName;

    @TableField("gender")
    private Integer gender;

    @TableField("avatar")
    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("birth")
    private LocalDate birth;

    @TableField("area")
    private String area;

    @TableField("introduction")
    private String introduction;
}
