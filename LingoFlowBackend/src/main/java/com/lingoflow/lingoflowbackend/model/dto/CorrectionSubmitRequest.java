package com.lingoflow.lingoflowbackend.model.dto;

import lombok.Data;

@Data
public class CorrectionSubmitRequest {
    private String type;
    private Long targetId;
    private String originalContent;
    private String userSuggestion;
}
