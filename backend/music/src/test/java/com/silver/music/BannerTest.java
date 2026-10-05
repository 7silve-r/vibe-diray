package com.silver.music;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.silver.diary.exception.BusinessException;
import com.silver.music.entity.Banner;
import com.silver.music.mapper.BannerMapper;
import com.silver.music.service.MinioService;
import com.silver.music.service.impl.BannerServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class BannerTest {
    @Test
    void rollback() {
        BannerMapper mapper = mock(BannerMapper.class);
        MinioService storage = mock(MinioService.class);
        BannerServiceImpl service = new BannerServiceImpl();
        ReflectionTestUtils.setField(service, "bannerMapper", mapper);
        ReflectionTestUtils.setField(service, "minioService", storage);
        Banner banner = new Banner();
        banner.setBannerUrl("http://localhost/image.png");
        when(mapper.selectByIds(List.of(1L))).thenReturn(List.of(banner));
        when(mapper.deleteByIds(List.of(1L))).thenReturn(0);
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertThrows(BusinessException.class, () -> service.deleteBanners(List.of(1L)));
            verifyNoInteractions(storage);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
