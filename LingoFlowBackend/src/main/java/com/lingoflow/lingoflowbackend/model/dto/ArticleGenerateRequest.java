package com.lingoflow.lingoflowbackend.model.dto;

import lombok.Data;

@Data
public class ArticleGenerateRequest {
    /**
     * 前端传来的原始长文本
     */
    private String originalText;

    /**
     * 设定的目标难度 (比如: V1000, CET4, N4 等)
     */
    private String difficultyLevel;
}