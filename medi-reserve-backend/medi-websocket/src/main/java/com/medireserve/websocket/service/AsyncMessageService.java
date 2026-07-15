package com.medireserve.websocket.service;

import com.medireserve.common.entity.ConsultationMessage;
import com.medireserve.websocket.mapper.ConsultationMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步消息处理服务
 * 将消息保存操作异步执行，不阻塞 WebSocket 主线程
 * 提升高并发场景下的消息吞吐量
 */
@Slf4j
@Service
public class AsyncMessageService {

    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    /**
     * 异步保存消息到数据库
     * 使用 @Async 标记，Spring 会在独立线程中执行
     *
     * @param message 消息实体
     */
    @Async
    public void saveMessageAsync(ConsultationMessage message) {
        try {
            consultationMessageMapper.insert(message);
            log.debug("消息异步保存成功，消息ID：{}", message.getId());
        } catch (Exception e) {
            // 异步操作中的异常不会影响主流程，但需要记录日志以便排查
            log.error("消息异步保存失败，发送者：{}，接收者：{}，预约：{}",
                    message.getSenderId(), message.getReceiverId(), message.getAppointmentId(), e);
        }
    }
}