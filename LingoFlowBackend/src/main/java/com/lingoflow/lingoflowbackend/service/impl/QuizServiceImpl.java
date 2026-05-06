package com.lingoflow.lingoflowbackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingoflow.lingoflowbackend.model.dto.QuizSubmitRequest;
import com.lingoflow.lingoflowbackend.model.entity.Article;
import com.lingoflow.lingoflowbackend.model.vo.QuizFeedbackVO;
import com.lingoflow.lingoflowbackend.model.vo.QuizQuestionVO;
import com.lingoflow.lingoflowbackend.service.ArticleService;
import com.lingoflow.lingoflowbackend.service.QuizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class QuizServiceImpl implements QuizService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<QuizQuestionVO> generateQuiz(Long userId, Long articleId) {
        log.info("【DEBUG】进入 generateQuiz 方法 - 用户ID: {}, 文章ID: {}", userId, articleId);
        System.out.println("【DEBUG】收到出题请求 - 文章ID: " + articleId);
        
        Article article = articleService.getById(articleId);
        if (article == null || !article.getUserId().equals(userId)) {
            throw new RuntimeException("文章不存在或无权访问");
        }

        String prompt = String.format("""
            You are an expert English teacher. Based on the following article, generate exactly 3 multiple-choice reading comprehension questions.
            
            CRITICAL INSTRUCTIONS:
            1. You MUST output ONLY valid JSON format. Do NOT use markdown code blocks (no ```json). Do NOT add any conversational filler.
            2. For each question, provide 4 options, the correct answer, and short explanations.
            
            REQUIRED JSON STRUCTURE (Array of objects):
            [
              {
                "question": "The question text?",
                "options": ["A. Option 1", "B. Option 2", "C. Option 3", "D. Option 4"],
                "correctAnswer": "A. Option 1",
                "explanation": "Short Chinese explanation of why this is correct",
                "englishExplanation": "Short English explanation (level: %s)"
              }
            ]
            
            ARTICLE CONTENT:
            %s
            """, article.getDifficultyLevel(), article.getAdaptedText());

        String aiResponse = "";
        try {
            aiResponse = chatClient.call(prompt);
            System.out.println("【DEBUG】AI 出题原始响应:\n" + aiResponse);
        } catch (Exception e) {
            log.error("AI 服务调用失败", e);
            throw new RuntimeException("AI 服务繁忙，请稍后再试");
        }

        return parseQuestionsFromText(aiResponse);
    }

    @Override
    public QuizFeedbackVO submitAndEvaluate(Long userId, QuizSubmitRequest request) {
        Article article = articleService.getById(request.getArticleId());
        if (article == null || !article.getUserId().equals(userId)) {
            throw new RuntimeException("文章不存在或无权访问");
        }

        try {
            String userAnswersStr = objectMapper.writeValueAsString(request.getUserAnswers());

            String prompt = String.format("""
                You are an expert English teacher. Evaluate the student's quiz answers based on the provided article.
                
                CRITICAL INSTRUCTIONS FOR AI:
                1. You MUST output ONLY valid JSON format. Do NOT use markdown code blocks (no ```json). Do NOT add any conversational filler before or after the JSON.
                2. Keep the explanations extremely short and concise to avoid generation errors.
                3. The total score must be between 0 and 100.
                
                REQUIRED JSON STRUCTURE:
                {
                  "score": 100,
                  "feedbacks": [
                    {
                      "question": "(Write the original question here)",
                      "userAnswer": "(Write the student's answer here)",
                      "isCorrect": true,
                      "explanation": "(Write short Chinese explanation here)",
                      "englishExplanation": "(Write short English explanation here, level: %s)"
                    }
                  ]
                }
                
                ARTICLE CONTENT:
                %s
                
                STUDENT ANSWERS:
                %s
                """,
                    article.getDifficultyLevel(),
                    article.getAdaptedText(),
                    userAnswersStr);

            System.out.println("【DEBUG】收到批改请求 - 文章ID: " + request.getArticleId());
            String aiResponse = "";
            try {
                aiResponse = chatClient.call(prompt);
                System.out.println("【DEBUG】AI 批改原始响应:\n" + aiResponse);
            } catch (Exception e) {
                log.error("AI 批改服务调用失败", e);
                throw new RuntimeException("AI 批改服务繁忙，请稍后再试");
            }

            return parseFeedbackFromText(aiResponse);

        } catch (Exception e) {
            log.error("AI 批改解析失败", e);
            throw new RuntimeException("AI 批改解析失败，请稍后重试");
        }
    }

    /**
     * 使用 JSON 解析题目
     */
    private List<QuizQuestionVO> parseQuestionsFromText(String text) {
        List<QuizQuestionVO> questions = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return questions;
        }
        
        try {
            int startIndex = text.indexOf("[");
            int endIndex = text.lastIndexOf("]");
            
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                String jsonStr = text.substring(startIndex, endIndex + 1);
                questions = objectMapper.readValue(jsonStr, new com.fasterxml.jackson.core.type.TypeReference<List<QuizQuestionVO>>(){});
                System.out.println("【DEBUG】成功解析出题目数量: " + questions.size());
            } else {
                log.warn("AI 回复中未找到有效的 JSON 数组结构: {}", text);
            }
        } catch (Exception e) {
            log.error("使用 ObjectMapper 解析 AI 出题 JSON 失败", e);
        }
        return questions;
    }

    /**
     * 使用 JSON 解析反馈
     */
    private QuizFeedbackVO parseFeedbackFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new QuizFeedbackVO();
        }
        
        try {
            // 尝试提取 JSON 内容，以防大模型还是输出了 markdown 或者废话
            int startIndex = text.indexOf("{");
            int endIndex = text.lastIndexOf("}");
            
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                String jsonStr = text.substring(startIndex, endIndex + 1);
                QuizFeedbackVO feedbackVO = objectMapper.readValue(jsonStr, QuizFeedbackVO.class);
                System.out.println("【DEBUG】成功解析出反馈条数: " + (feedbackVO.getFeedbacks() != null ? feedbackVO.getFeedbacks().size() : 0));
                return feedbackVO;
            } else {
                log.warn("AI 回复中未找到有效的 JSON 结构: {}", text);
            }
        } catch (Exception e) {
            log.error("使用 ObjectMapper 解析 AI JSON 反馈失败", e);
        }
        
        System.out.println("【DEBUG】成功解析出反馈条数: 0");
        return new QuizFeedbackVO();
    }

    private String extractTagContent(String text, String tag) {
        // 匹配标签到下一行或下一个标签或结尾
        Pattern pattern = Pattern.compile(tag + "\\s*(.*?)(?=\\s*\\[|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String extractTagContent(String text, String startTag, String endTag) {
        Pattern pattern = Pattern.compile(startTag + "(.*?)" + endTag, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}