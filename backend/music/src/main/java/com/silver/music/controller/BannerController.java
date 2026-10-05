package com.silver.music.controller;

import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.BannerQueryDto;
import com.silver.music.entity.Banner;
import com.silver.music.service.BannerService;
import com.silver.music.vo.BannerVo;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class BannerController {

    @Autowired private BannerService bannerService;

    @PostMapping("/music/admin/listBanners")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Banner>> listBanners(@RequestBody @Valid BannerQueryDto bannerDto) {
        return bannerService.listBanners(bannerDto);
    }

    @PatchMapping("/music/admin/updateBannerStatus/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateBannerStatus(
            @PathVariable("id") Long bannerId, @RequestParam("status") Integer bannerStatus) {
        return bannerService.updateBannerStatus(bannerId, bannerStatus);
    }

    @DeleteMapping("/music/admin/deleteBanner/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBanner(@PathVariable("id") Long bannerId) {
        return bannerService.deleteBanner(bannerId);
    }

    @DeleteMapping("/music/admin/deleteBanners")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBanners(@RequestBody List<Long> bannerIds) {
        return bannerService.deleteBanners(bannerIds);
    }

    @GetMapping("/music/public/banner/getBannerList")
    public Result<List<BannerVo>> getBannerList() {
        return bannerService.getBannerList();
    }
}
