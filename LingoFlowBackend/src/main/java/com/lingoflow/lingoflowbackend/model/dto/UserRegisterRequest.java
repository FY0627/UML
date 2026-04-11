package com.lingoflow.lingoflowbackend.model.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String username;
    private String password;
    private String targetLanguage; // 比如 EN
    private String currentLevel;   // 比如 V1000
    private String userRole;       // USER or ADMIN
}