package com.medireserve.websocket.controller;

import com.medireserve.common.constant.MessageTypeConstant;
import com.medireserve.common.dto.ChatMessageVO;
import com.medireserve.common.dto.SendMessageDTO;
import com.medireserve.common.entity.Appointment;
import com.medireserve.common.entity.ConsultationMessage;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.Patient;
import com.medireserve.common.exception.ConsultationException;
import com.medireserve.common.mapper.DoctorAuthMapper;
import com.medireserve.common.mapper.PatientAuthMapper;
import com.medireserve.websocket.mapper.ConsultationMessageMapper;
import com.medireserve.websocket.service.ConsultationRedisService;
import com.medireserve.websocket.service.ConsultationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket 消息处理控制器
 * 处理 /app 前缀的 STOMP 消息
 */
@Slf4j
@Controller
public class ChatController {

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private ConsultationRedisService consultationRedisService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    @Autowired
    private DoctorAuthMapper doctorAuthMapper;

    @Autowired
    private PatientAuthMapper patientAuthMapper;

    /**
     * 发送消息（核心方法）
     * 前端通过 /app/chat.send 发送 STOMP 消息
     *
     * 消息路由：
     * 1. 广播到房间频道 /topic/room/{appointmentId}（按预约隔离，推荐）
     * 2. 点对点推送 /user/{receiverId}/queue/messages（备用）
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageDTO sendMessageDTO,
                            SimpMessageHeaderAccessor headerAccessor) {

        // ========== 1. 认证校验 ==========
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String senderRole = (String) headerAccessor.getSessionAttributes().get("role");

        if (senderId == null || senderRole == null) {
            throw new ConsultationException("会话信息缺失，请重新连接");
        }

        Long appointmentId = sendMessageDTO.getAppointmentId();
        Long receiverId = sendMessageDTO.getReceiverId();

        log.info("收到消息：发送者 {}，接收者 {}，预约 {}", senderId, receiverId, appointmentId);

        // ========== 2. 业务校验 ==========
        Appointment appointment = consultationService.checkConsultationAccess(appointmentId, senderId, senderRole);

        // ========== 3. 接收者校验 ==========
        boolean isPatient = "PATIENT".equals(senderRole);
        if (isPatient) {
            if (!receiverId.equals(appointment.getDoctorId())) {
                throw new ConsultationException("您只能发送消息给您的医生");
            }
        } else {
            if (!receiverId.equals(appointment.getPatientId())) {
                throw new ConsultationException("您只能发送消息给您的患者");
            }
        }

        // ========== 4. 防 XSS 过滤 ==========
        String safeContent = sendMessageDTO.getContent()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");

        // ========== 5. 构建消息实体 ==========
        ConsultationMessage message = new ConsultationMessage();
        message.setAppointmentId(appointmentId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSenderRole(senderRole);
        message.setContent(safeContent);
        message.setMsgType(sendMessageDTO.getMsgType() == null ? MessageTypeConstant.TEXT : sendMessageDTO.getMsgType());
        message.setSendTime(LocalDateTime.now());

        // ========== 6. 同步保存到数据库 ==========
        consultationMessageMapper.insert(message);
        log.debug("消息保存成功，消息ID：{}", message.getId());

        // ========== 7. 查询发送者真实姓名 ==========
        String senderName;
        if (isPatient) {
            Patient patient = patientAuthMapper.findById(senderId);
            senderName = patient != null ? patient.getName() : "患者";
        } else {
            Doctor doctor = doctorAuthMapper.findById(senderId);
            senderName = doctor != null ? doctor.getName() : "医生";
        }

        // ========== 8. 构建 VO ==========
        ChatMessageVO chatMessageVO = new ChatMessageVO();
        chatMessageVO.setMessageId(message.getId());
        chatMessageVO.setSenderId(senderId);
        chatMessageVO.setSenderName(senderName);
        chatMessageVO.setSenderRole(senderRole);
        chatMessageVO.setContent(safeContent);
        chatMessageVO.setSendTime(message.getSendTime());
        //chatMessageVO.setIsSelf(false);

        // ================================================================
        //  ========== 9. 广播到房间频道（按预约ID隔离） ==========
        //  所有订阅了 /topic/room/{appointmentId} 的用户都能收到
        //  实现了按问诊室隔离的效果
        // ================================================================
        String roomTopic = "/topic/room/" + appointmentId;
        messagingTemplate.convertAndSend(roomTopic, chatMessageVO);
        log.info("消息已广播到房间频道 {}", roomTopic);

        // ========== 10. 点对点推送（备用，兼容旧客户端） ==========
        // 2. 仅当接收者离线时，才使用点对点渠道推送离线消息（上线后补发）
        boolean isReceiverOnline = consultationRedisService.isOnline(receiverId);
        if (!isReceiverOnline) {
            consultationRedisService.storeOfflineMessage(receiverId, chatMessageVO);
            log.info("用户 {} 离线，消息已暂存", receiverId);
        }
    }
}