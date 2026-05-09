package com.lingoflow.lingoflowbackend.model.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String targetLanguage;
    private String currentLevel;
    private String userRole;
}