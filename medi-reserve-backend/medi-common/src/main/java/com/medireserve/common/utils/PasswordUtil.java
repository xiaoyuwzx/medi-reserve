package com.medireserve.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密工具类
 * 特点：每次加密结果不同，但校验时能正确匹配
 */
@Slf4j
public class PasswordUtil {

    /**
     * BCrypt 强度系数（4~31），默认 10
     * 值越大，加密耗时越长，安全性越高
     */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    /**
     * 加密明文密码
     * @param rawPassword 明文密码
     * @return BCrypt 加密后的密文
     */
    public static String encode(String rawPassword) {
        String encoded = ENCODER.encode(rawPassword);
        log.debug("密码加密成功");
        return encoded;
    }

    /**
     * 校验明文密码与密文是否匹配
     * @param rawPassword 明文密码
     * @param encodedPassword BCrypt 加密后的密文
     * @return true=匹配成功，false=匹配失败
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        boolean result = ENCODER.matches(rawPassword, encodedPassword);
        if (!result) {
            log.warn("密码校验失败");
        }
        return result;
    }
}