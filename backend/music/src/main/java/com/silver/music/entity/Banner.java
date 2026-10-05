package com.silver.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silver.music.enumeration.BannerStatusEnum;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("tb_banner")
public class Banner implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long bannerId;

    @TableField("banner_url")
    private String bannerUrl;

    @TableField("status")
    private BannerStatusEnum bannerStatus;
}
