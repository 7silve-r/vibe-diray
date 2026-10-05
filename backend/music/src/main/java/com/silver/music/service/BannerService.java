package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.BannerQueryDto;
import com.silver.music.entity.Banner;
import com.silver.music.vo.BannerVo;
import java.util.List;

public interface BannerService extends IService<Banner> {

    Result<PageResult<Banner>> listBanners(BannerQueryDto bannerDto);

    Result<Void> addBanner(String bannerUrl);

    Result<Void> updateBanner(Long bannerId, String bannerUrl);

    Result<Void> updateBannerStatus(Long bannerId, Integer bannerStatus);

    Result<Void> deleteBanner(Long bannerId);

    Result<Void> deleteBanners(List<Long> bannerIds);

    Result<List<BannerVo>> getBannerList();
}
