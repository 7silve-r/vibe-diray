package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.music.entity.PlaylistBinding;

public interface PlaylistBindingService extends IService<PlaylistBinding> {

    void update(Long id, java.util.List<Long> songIds);
}
