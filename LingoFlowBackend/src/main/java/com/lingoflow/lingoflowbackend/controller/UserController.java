package com.lingoflow.lingoflowbackend.controller;

import com.lingoflow.lingoflowbackend.common.Result;
import com.lingoflow.lingoflowbackend.model.dto.UserLoginRequest;
import com.lingoflow.lingoflowbackend.model.dto.UserRegisterRequest;
import com.lingoflow.lingoflowbackend.service.UserService;
import com.lingoflow.lingoflowbackend.model.entity.User;
import com.lingoflow.lingoflowbackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody UserRegisterRequest request) {
        Long userId = userService.register(request);
        return Result.success(userId);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request);
        return Result.success(token);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        // 1. 从刚才的拦截器里，获取存入的 userId
        Long userId = (Long) request.getAttribute("userId");

        // 2. 去数据库查询对应的用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 封装到 VO 中返回，屏蔽掉敏感信息（比如密码、逻辑删除字段等）
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setTargetLanguage(user.getTargetLanguage());
        userVO.setCurrentLevel(user.getCurrentLevel());
        userVO.setUserRole(user.getUserRole());

        return Result.success(userVO);
    }
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody com.lingoflow.lingoflowbackend.model.dto.ChangePasswordRequest changePasswordRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, changePasswordRequest);
        return Result.success("修改成功");
    }
}