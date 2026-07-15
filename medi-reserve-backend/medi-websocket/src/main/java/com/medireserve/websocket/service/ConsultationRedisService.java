package com.medireserve.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 问诊会话管理（Redis）
 * 负责：在线状态、房间成员、离线消息
 */
@Slf4j
@Service
public class ConsultationRedisService {

    private static final String USER_SESSION_KEY = "ws:user:%s";         // 用户会话
    private static final String ROOM_MEMBERS_KEY = "ws:room:%s";         // 房间成员
    private static final String OFFLINE_MSG_KEY = "ws:offline:%s";       // 离线消息

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 会话管理 ====================

    /**
     * 用户上线：记录 sessionId
     */
    public void userOnline(Long userId, String sessionId) {
        String key = String.format(USER_SESSION_KEY, userId);
        redisTemplate.opsForValue().set(key, sessionId, 24, TimeUnit.HOURS);
        log.debug("用户 {} 上线，sessionId：{}", userId, sessionId);
    }

    /**
     * 用户下线：删除 sessionId
     */
    public void userOffline(Long userId) {
        String key = String.format(USER_SESSION_KEY, userId);
        redisTemplate.delete(key);
        log.debug("用户 {} 下线", userId);
    }

    /**
     * 判断用户是否在线
     */
    public boolean isOnline(Long userId) {
        String key = String.format(USER_SESSION_KEY, userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取用户的 sessionId
     */
    public String getSessionId(Long userId) {
        String key = String.format(USER_SESSION_KEY, userId);
        return (String) redisTemplate.opsForValue().get(key);
    }

    // ==================== 房间管理 ====================

    /**
     * 用户进入房间
     */
    public void joinRoom(Long appointmentId, Long userId) {
        String key = String.format(ROOM_MEMBERS_KEY, appointmentId);
        redisTemplate.opsForSet().add(key, userId.toString());
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    /**
     * 用户离开房间
     */
    public void leaveRoom(Long appointmentId, Long userId) {
        String key = String.format(ROOM_MEMBERS_KEY, appointmentId);
        redisTemplate.opsForSet().remove(key, userId.toString());
    }

    /**
     * 获取房间在线人数
     */
    public int getRoomOnlineCount(Long appointmentId) {
        String key = String.format(ROOM_MEMBERS_KEY, appointmentId);
        Set<Object> members = redisTemplate.opsForSet().members(key);
        return members != null ? members.size() : 0;
    }

    // ==================== 离线消息 ====================

    /**
     * 存储离线消息（当接收者不在线时调用）
     */
    public void storeOfflineMessage(Long receiverId, Object message) {
        String key = String.format(OFFLINE_MSG_KEY, receiverId);
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
        log.debug("存储离线消息给用户 {}，消息内容：{}", receiverId, message);
    }

    /**
     * 获取并清空离线消息（用户上线时调用）
     */
    public java.util.List<Object> getAndClearOfflineMessages(Long userId) {
        String key = String.format(OFFLINE_MSG_KEY, userId);
        java.util.List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
        redisTemplate.delete(key);
        if (messages != null && !messages.isEmpty()) {
            log.debug("用户 {} 获取离线消息 {} 条", userId, messages.size());
        }
        return messages;
    }
}