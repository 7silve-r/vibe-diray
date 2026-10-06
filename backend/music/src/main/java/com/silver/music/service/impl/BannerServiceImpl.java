package com.silver.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.silver.diary.common.PageResult;
import com.silver.diary.exception.BusinessException;
import com.silver.music.dto.BannerQueryDto;
import com.silver.music.entity.Banner;
import com.silver.music.enumeration.BannerStatus;
import com.silver.music.mapper.BannerMapper;
import com.silver.music.service.BannerService;
import com.silver.music.service.MinioService;
import com.silver.music.upload.UploadCleanup;
import com.silver.music.vo.BannerVo;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Autowired private BannerMapper bannerMapper;
    @Autowired private MinioService minioService;

    @Override
    public PageResult<Banner> listBanners(BannerQueryDto bannerDto) {

        Page<Banner> page = new Page<>(bannerDto.getPageNum(), bannerDto.getPageSize());
        LambdaQueryWrapper<Banner> query = new LambdaQueryWrapper<>();
        if (bannerDto.getBannerStatus() != null) {
            query.eq(Banner::getBannerStatus, bannerDto.getBannerStatus().getId());
        }

        query.orderByDesc(Banner::getBannerId);

        IPage<Banner> bannerPage = bannerMapper.selectPage(page, query);

        return new PageResult<>(bannerPage.getTotal(), bannerPage.getRecords());
    }

    @Override
    @Transactional
    public void addBanner(String bannerUrl) {
        Banner banner = new Banner();
        banner.setBannerUrl(bannerUrl);
        banner.setBannerStatus(BannerStatus.ENABLE);

        if (bannerMapper.insert(banner) == 0) {
            throw new BusinessException("添加失败");
        }
    }

    @Override
    @Transactional
    public void updateBanner(Long bannerId, String bannerUrl) {
        Banner banner = bannerMapper.selectById(bannerId);
        if (banner == null) throw new BusinessException(404, "资源不存在");
        String oldBannerUrl = banner.getBannerUrl();

        banner.setBannerUrl(bannerUrl);
        if (bannerMapper.updateById(banner) == 0) {
            throw new BusinessException("更新失败");
        }

        UploadCleanup.afterCommit(minioService, oldBannerUrl);
    }

    @Override
    @Transactional
    public void updateBannerStatus(Long bannerId, Integer bannerStatus) {

        BannerStatus statusEnum;
        if (bannerStatus == 0) {
            statusEnum = BannerStatus.ENABLE;
        } else if (bannerStatus == 1) {
            statusEnum = BannerStatus.DISABLE;
        } else {
            throw new BusinessException("轮播图状态无效");
        }

        Banner banner = new Banner();
        banner.setBannerId(bannerId);
        banner.setBannerStatus(statusEnum);

        if (bannerMapper.updateById(banner) == 0) {
            throw new BusinessException("更新失败");
        }
    }

    @Override
    @Transactional
    public void deleteBanner(Long bannerId) {
        Banner banner = bannerMapper.selectById(bannerId);
        if (banner == null) {
            throw new BusinessException("未找到相关数据");
        }
        String bannerUrl = banner.getBannerUrl();
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            UploadCleanup.afterCommit(minioService, bannerUrl);
        }

        if (bannerMapper.deleteById(bannerId) == 0) {
            throw new BusinessException("删除失败");
        }
    }

    @Override
    @Transactional
    public void deleteBanners(List<Long> bannerIds) {
        List<Banner> banners = bannerMapper.selectByIds(bannerIds);
        List<String> bannerUrlList =
                banners.stream()
                        .map(Banner::getBannerUrl)
                        .filter(url -> url != null && !url.isEmpty())
                        .toList();
        bannerUrlList.forEach(url -> UploadCleanup.afterCommit(minioService, url));

        if (bannerMapper.deleteByIds(bannerIds) == 0) {
            throw new BusinessException("删除失败");
        }
    }

    @Override
    public List<BannerVo> getBannerList() {

        List<Banner> banners =
                bannerMapper.selectList(
                        new LambdaQueryWrapper<Banner>()
                                .eq(Banner::getBannerStatus, BannerStatus.ENABLE.getId())
                                .orderByDesc(Banner::getBannerId)
                                .last("limit 9"));

        List<BannerVo> bannerVoList =
                banners.stream()
                        .map(
                                banner -> {
                                    BannerVo bannerVo = new BannerVo();
                                    BeanUtils.copyProperties(banner, bannerVo);
                                    return bannerVo;
                                })
                        .toList();

        return bannerVoList;
    }
}
