package com.lingoflow.lingoflowbackend.model.dto;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String username;
    private String password;
    private String userRole; // 用户登录时选择的角色，用于鉴权
}
