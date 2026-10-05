package com.silver.diary.service;

import com.silver.diary.dto.EmailDto;

public interface EmailService {
    void send(EmailDto dto);

    void bind(EmailDto dto);

    void reset(EmailDto dto);
}
