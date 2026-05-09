package com.lingoflow.lingoflowbackend.model.dto;

import lombok.Data;

@Data
public class VocabularyAddRequest {
    private Long articleId;
    private String word;
    private String translation;
    private String contextSentence;
}