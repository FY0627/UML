package com.lingoflow.lingoflowbackend.service;

import com.lingoflow.lingoflowbackend.model.dto.QuizSubmitRequest;
import com.lingoflow.lingoflowbackend.model.vo.QuizFeedbackVO;
import com.lingoflow.lingoflowbackend.model.vo.QuizQuestionVO;

import java.util.List;

public interface QuizService {
    // 动态生成测试题
    List<QuizQuestionVO> generateQuiz(Long userId, Long articleId);

    // 提交答案，AI 动态批改
    QuizFeedbackVO submitAndEvaluate(Long userId, QuizSubmitRequest request);
}