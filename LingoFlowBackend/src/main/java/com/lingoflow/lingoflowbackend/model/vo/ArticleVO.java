package com.lingoflow.lingoflowbackend.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    private String originalText;
    private String adaptedText;
    private String targetLanguage;
    private String difficultyLevel;
    private LocalDateTime createTime;
}