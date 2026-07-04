package com.medireserve.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

    // 从 application.yml 读取配置
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;  // 单位：毫秒

    /**
     * 生成签名密钥（基于 HS256 算法）
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 创建令牌，存储用户 id、name、role
     * @param id   用户ID
     * @param name 用户名
     * @param role     角色（如 PATIENT、DOCTOR、ADMIN）
     * @return JWT 字符串
     */
    public String createToken(Long id, String name, String role) {
        // 构建 Claims（载荷）
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("name", name);
        claims.put("role", role);

        String token = Jwts.builder()
                .claims(claims)                         // 设置自定义数据
                .issuedAt(new Date())                   // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())                 // 签名
                .compact();

        log.info("生成令牌成功，id: {}, 用户: {}, 角色: {}", id, name, role);
        return token;
    }

    /**
     * 解析令牌，返回 Claims（包含所有数据）
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // -------- 快捷提取方法，方便后续使用 ----------
    public Long getId(String token) {
        return parseToken(token).get("id", Long.class);
    }

    public String getName(String token) {
        return parseToken(token).get("name", String.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}