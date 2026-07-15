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

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket 消息处理控制器
 * 处理 /app 前缀的 STOMP 消息
 *
 * 核心功能：
 * 1. 接收前端发送的消息
 * 2. 校验发送者身份和权限
 * 3. 保存消息到数据库（同步，确保ID回填）
 * 4. 实时推送给接收者（在线）或暂存离线消息（离线）
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
     * 处理流程：
     * 1. 认证校验（从拦截器存入的 session 中提取用户信息）
     * 2. 业务校验（预约归属、状态、日期）
     * 3. 接收者校验（防止发错人）
     * 4. 同步保存到数据库（确保 ID 回填，用于前端展示）
     * 5. 查询发送者真实姓名（提升用户体验）
     * 6. 实时推送（在线）或暂存离线消息（离线）
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageDTO sendMessageDTO,
                            SimpMessageHeaderAccessor headerAccessor,
                            Principal principal) {

        // ========== 1. 认证校验 ==========
        if (principal == null) {
            throw new ConsultationException("未认证的用户");
        }

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

        // ========== 3. 接收者校验（防止发错人） ==========
        boolean isPatient = "PATIENT".equals(senderRole);
        if (isPatient) {
            // 患者只能发给自己的医生
            if (!receiverId.equals(appointment.getDoctorId())) {
                throw new ConsultationException("您只能发送消息给您的医生");
            }
        } else {
            // 医生只能发给自己的患者
            if (!receiverId.equals(appointment.getPatientId())) {
                throw new ConsultationException("您只能发送消息给您的患者");
            }
        }

        // ========== 4. 防 XSS 过滤（将 HTML 标签转义为安全字符） ==========
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

        // ========== 6. 同步保存到数据库（确保 MyBatis 回填 ID） ==========
        // 注意：改为同步保存是为了立即获取 messageId，便于前端做消息去重和引用
        // 消息保存通常耗时 20-50ms，对用户体验影响可以接受
        consultationMessageMapper.insert(message);
        log.debug("消息保存成功，消息ID：{}", message.getId());

        // ========== 7. 查询发送者真实姓名（提升用户体验） ==========
        String senderName;
        if (isPatient) {
            Patient patient = patientAuthMapper.findById(senderId);
            senderName = patient != null ? patient.getName() : "患者";
        } else {
            Doctor doctor = doctorAuthMapper.findById(senderId);
            senderName = doctor != null ? doctor.getName() : "医生";
        }

        // ========== 8. 构建 VO 用于推送给接收者 ==========
        ChatMessageVO chatMessageVO = new ChatMessageVO();
        chatMessageVO.setMessageId(message.getId());
        chatMessageVO.setSenderId(senderId);
        chatMessageVO.setSenderName(senderName);
        chatMessageVO.setSenderRole(senderRole);
        chatMessageVO.setContent(safeContent);
        chatMessageVO.setSendTime(message.getSendTime());
        chatMessageVO.setIsSelf(false);  // 接收者收到时是 false

        // ========== 9. 判断接收者状态，推送或暂存 ==========
        boolean isReceiverOnline = consultationRedisService.isOnline(receiverId);

        if (isReceiverOnline) {
            // 接收者在线：实时推送（通过 STOMP 点对点）
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/messages",
                    chatMessageVO
            );
            log.info("消息已实时推送给用户 {}", receiverId);
        } else {
            // 接收者离线：暂存到 Redis，待上线后推送
            consultationRedisService.storeOfflineMessage(receiverId, chatMessageVO);
            log.info("用户 {} 离线，消息已暂存", receiverId);
        }
    }
}