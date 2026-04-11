package com.lingoflow.lingoflowbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lingoflow.lingoflowbackend.model.dto.UserLoginRequest;
import com.lingoflow.lingoflowbackend.model.dto.UserRegisterRequest;
import com.lingoflow.lingoflowbackend.model.entity.User;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @return 新用户的 ID
     */
    Long register(UserRegisterRequest request);

    /**
     * 用户登录
     * @return JWT Token 字符串
     */
    String login(UserLoginRequest request);
    /**
     * 修改密码
     */
    void changePassword(Long userId, com.lingoflow.lingoflowbackend.model.dto.ChangePasswordRequest request);
}