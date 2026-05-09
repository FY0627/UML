package com.lingoflow.lingoflowbackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizSubmitRequest {
    private Long articleId;
    private List<UserAnswer> userAnswers;

    @Data
    public static class UserAnswer {
        private String question;
        private List<String> originalOptions;
        private String selectedOption;
    }
}