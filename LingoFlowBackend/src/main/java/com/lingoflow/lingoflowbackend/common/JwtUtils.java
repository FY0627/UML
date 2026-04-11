package com.lingoflow.lingoflowbackend.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtils {

    // 密钥 (注意：实际企业开发中，这个密钥应该配置在 application.yml 中，并足够复杂)
    private static final String SECRET = "LingoFlowSecretKey2026@#$!";

    // Token 有效期：这里设置为 7 天 (单位：毫秒)
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 根据用户 ID 和用户名生成 Token
     */
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims) // 设置载荷(Payload)
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET) // 签名算法和密钥
                .compact();
    }

    /**
     * 解析 Token 获取载荷 (Claims)
     * 如果解析失败（如 Token 过期或被篡改），会自动抛出异常
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
