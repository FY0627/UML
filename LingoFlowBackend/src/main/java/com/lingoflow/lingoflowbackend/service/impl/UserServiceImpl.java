package com.lingoflow.lingoflowbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingoflow.lingoflowbackend.common.JwtUtils;
import com.lingoflow.lingoflowbackend.mapper.UserMapper;
import com.lingoflow.lingoflowbackend.model.dto.UserLoginRequest;
import com.lingoflow.lingoflowbackend.model.dto.UserRegisterRequest;
import com.lingoflow.lingoflowbackend.model.entity.User;
import com.lingoflow.lingoflowbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 密码加密的盐值（提高安全性，防止彩虹表破解）
    private static final String SALT = "lingo_flow_2026";

    @Override
    public Long register(UserRegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // 1. 校验参数是否为空
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new RuntimeException("用户名或密码不能为空"); // 这里的异常会被咱们的 GlobalExceptionHandler 拦截
        }

        // 2. 校验用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 3. 密码加密 (MD5 + 盐)
        String md5Password = DigestUtils.md5DigestAsHex((password + SALT).getBytes(StandardCharsets.UTF_8));

        // 4. 组装实体对象并保存到数据库
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(md5Password);
        user.setTargetLanguage(request.getTargetLanguage() != null ? request.getTargetLanguage() : "EN");
        user.setCurrentLevel(request.getCurrentLevel() != null ? request.getCurrentLevel() : "V1000");
        user.setUserRole(request.getUserRole() != null ? request.getUserRole() : "USER");

        this.save(user); // MyBatis-Plus 提供的方法，插入成功后会自动把主键 ID 回写到 user 对象中

        return user.getId();
    }

    @Override
    public String login(UserLoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // 1. 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = this.getOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 校验密码
        String md5Password = DigestUtils.md5DigestAsHex((password + SALT).getBytes(StandardCharsets.UTF_8));
        if (!user.getPasswordHash().equals(md5Password)) {
            throw new RuntimeException("密码错误");
        }

        // 3. 校验角色身份是否匹配
        // 如果用户在前端选的是“管理员”，但数据库里是“USER”，则拒绝登录
        if (request.getUserRole() != null && !request.getUserRole().equalsIgnoreCase(user.getUserRole())) {
            throw new RuntimeException("登录身份不匹配，请检查选择的角色");
        }

        // 3. 登录成功，颁发 JWT Token
        return JwtUtils.generateToken(user.getId(), user.getUsername());
    }
    @Override
    public void changePassword(Long userId, com.lingoflow.lingoflowbackend.model.dto.ChangePasswordRequest request) {
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        if (oldPassword == null || oldPassword.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("原密码和新密码不能为空");
        }

        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码
        String oldMd5 = DigestUtils.md5DigestAsHex((oldPassword + SALT).getBytes(StandardCharsets.UTF_8));
        if (!user.getPasswordHash().equals(oldMd5)) {
            throw new RuntimeException("原密码错误");
        }

        // 更新为新密码
        String newMd5 = DigestUtils.md5DigestAsHex((newPassword + SALT).getBytes(StandardCharsets.UTF_8));
        user.setPasswordHash(newMd5);
        this.updateById(user);
    }
}