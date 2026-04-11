package com.lingoflow.lingoflowbackend.interceptor;

import com.lingoflow.lingoflowbackend.common.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 统一拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 放行前端跨域的预检请求 (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 从请求头中获取 Token (前端通常会放在 Authorization 头里，并加上 "Bearer " 前缀)
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未登录或请求头格式错误，请重新登录"); // 抛出异常，由全局异常处理器接管
        }

        // 3. 提取并验证 Token
        String token = authHeader.substring(7); // 截取 "Bearer " 后面的真实 Token 字符串
        try {
            // 解析 Token，如果过期或被篡改，这一步会直接报错走 catch
            Claims claims = JwtUtils.parseToken(token);

            // 4. 【核心逻辑】解析成功！将 Token 里藏着的 userId 拿出来，存到本次请求的 request 对象里
            Long userId = claims.get("userId", Long.class);
            request.setAttribute("userId", userId);

            return true; // 验证通过，放行请求！
        } catch (Exception e) {
            throw new RuntimeException("Token无效或已过期，请重新登录");
        }
    }
}