package com.medireserve.common.service;

import com.medireserve.common.exception.AccountLockedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 登录成功：清除失败计数
     */
    public void loginSucceeded(String username) {
        String key = buildKey(username);
        redisTemplate.delete(key);
        log.debug("登录成功，清除失败计数，key: {}", key);
    }

    /**
     * 登录失败：增加失败计数，并判断是否达到锁定阈值
     */
    public void loginFailed(String username) {
        String key = buildKey(username);
        // increment 返回 Long，自动拆箱为 long，然后转 int
        Long countLong = redisTemplate.opsForValue().increment(key);
        int attempts = countLong.intValue();

        if (attempts == 1) {
            // 第一次失败，设置过期时间
            redisTemplate.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }

        if (attempts >= MAX_ATTEMPTS) {
            log.warn("账号 {} 因登录失败 {} 次被锁定，锁定 {} 分钟", username, attempts, LOCK_DURATION_MINUTES);
            throw new AccountLockedException(
                    "账号因连续登录失败 " + MAX_ATTEMPTS + " 次被锁定，请 " + LOCK_DURATION_MINUTES + " 分钟后重试"
            );
        }

        log.debug("账号 {} 登录失败，当前失败次数：{}", username, attempts);
    }

    /**
     * 登录前检查账号是否已被锁定
     */
    public void checkAttempts(String username) {
        String key = buildKey(username);
        String attemptsStr = redisTemplate.opsForValue().get(key);
        if (attemptsStr != null) {
            int attempts = Integer.parseInt(attemptsStr);
            if (attempts >= MAX_ATTEMPTS) {
                log.warn("账号 {} 已被锁定，剩余锁定时间：{} 秒", username,
                        redisTemplate.getExpire(key, TimeUnit.SECONDS));
                throw new AccountLockedException(
                        "账号已被锁定，请 " + LOCK_DURATION_MINUTES + " 分钟后重试"
                );
            }
        }
    }

    private String buildKey(String username) {
        return "login:fail:" + username;
    }
}