package com.lingoflow.lingoflowbackend.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VocabularyVO {
    private Long id;
    private Long articleId;
    private String word;
    private String translation;
    private String contextSentence;
    private Integer mastered;
    private LocalDateTime createTime;
}