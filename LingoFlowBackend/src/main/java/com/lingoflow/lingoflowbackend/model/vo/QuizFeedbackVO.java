package com.lingoflow.lingoflowbackend.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class QuizFeedbackVO {
    private Integer score;
    private List<FeedbackItem> feedbacks;

    @Data
    public static class FeedbackItem {
        private String question;
        private String userAnswer;
        private Boolean isCorrect;
        private String explanation;
        private String englishExplanation;
    }
}