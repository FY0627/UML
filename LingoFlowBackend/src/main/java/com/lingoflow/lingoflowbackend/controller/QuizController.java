package com.lingoflow.lingoflowbackend.controller;

import com.lingoflow.lingoflowbackend.common.Result;
import com.lingoflow.lingoflowbackend.model.dto.QuizSubmitRequest;
import com.lingoflow.lingoflowbackend.model.vo.QuizFeedbackVO;
import com.lingoflow.lingoflowbackend.model.vo.QuizQuestionVO;
import com.lingoflow.lingoflowbackend.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    /**
     * 根据文章 ID 动态生成 3 道测试题
     */
    @PostMapping("/generate/{articleId}")
    public Result<List<QuizQuestionVO>> generateQuiz(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<QuizQuestionVO> quizList = quizService.generateQuiz(userId, articleId);
        return Result.success(quizList);
    }

    /**
     * 提交答卷，获取 AI 判卷评分和解析
     */
    @PostMapping("/submit")
    public Result<QuizFeedbackVO> submitQuiz(@RequestBody QuizSubmitRequest submitRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        QuizFeedbackVO feedbackVO = quizService.submitAndEvaluate(userId, submitRequest);
        return Result.success(feedbackVO);
    }
}