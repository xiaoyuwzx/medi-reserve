package com.medireserve.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类：生成令牌、解析令牌、从令牌中提取用户信息
 */
@Slf4j
@Component
public class JwtUtil {

    // ========== 静态变量（存储配置） ==========
    private static String SECRET;
    private static Long EXPIRATION;

    // ========== 从配置文件读取（通过 @Value 注入实例变量） ==========
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Spring 容器启动时，将配置值赋给静态变量
     */
    @PostConstruct
    public void init() {
        SECRET = this.secret;
        EXPIRATION = this.expiration;
        log.info("JWT 配置加载成功，过期时间：{} 毫秒", EXPIRATION);
    }

    /**
     * 生成签名密钥（基于 HS256 算法）
     */
    private static SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 创建令牌，存储用户 id、name、role
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（如 PATIENT、DOCTOR、ADMIN）
     * @return JWT 字符串
     */
    public static String createToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSignKey())
                .compact();

        log.debug("生成令牌成功，用户：{}，角色：{}", username, role);
        return token;
    }

    /**
     * 解析令牌，返回 Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取用户ID
     */
    public static Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从令牌中获取用户名
     */
    public static String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    /**
     * 从令牌中获取角色
     */
    public static String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

}