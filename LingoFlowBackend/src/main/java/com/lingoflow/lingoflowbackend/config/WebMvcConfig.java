package com.lingoflow.lingoflowbackend.config;

import com.lingoflow.lingoflowbackend.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")       // 拦截所有 /api 开头的请求
                .excludePathPatterns(             // 白名单：放行以下接口
                        "/api/user/login",        // 登录
                        "/api/user/register",     // 注册
                        "/api/test/**"            // 之前的测试接口也放行
                );
    }
}