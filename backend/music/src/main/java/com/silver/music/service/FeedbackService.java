package com.silver.music.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.silver.diary.common.PageResult;
import com.silver.diary.common.Result;
import com.silver.music.dto.FeedbackQueryDto;
import com.silver.music.entity.Feedback;
import java.util.List;

public interface FeedbackService extends IService<Feedback> {

    Result<PageResult<Feedback>> listFeedback(FeedbackQueryDto feedbackDto);

    Result<Void> deleteFeedback(Long feedbackId);

    Result<Void> deleteFeedbacks(List<Long> feedbackIds);

    Result<Void> addFeedback(String content);
}
