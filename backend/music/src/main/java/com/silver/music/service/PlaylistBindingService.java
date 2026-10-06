package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.music.entity.PlaylistBinding;
import java.util.List;

public interface PlaylistBindingService extends IService<PlaylistBinding> {

    void update(Long id, List<Long> songIds);
}
