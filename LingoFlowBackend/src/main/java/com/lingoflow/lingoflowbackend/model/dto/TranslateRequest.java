package com.lingoflow.lingoflowbackend.model.dto;

import lombok.Data;

@Data
public class TranslateRequest {
    /**
     * 用户划选的生词
     */
    private String word;

    /**
     * 该生词在文章中的上下文原句
     */
    private String contextSentence;
}