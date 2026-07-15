package com.medireserve.websocket.mapper;

import com.medireserve.common.dto.ChatMessageVO;
import com.medireserve.common.entity.ConsultationMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsultationMessageMapper {

    /**
     * 插入消息
     */
    int insert(ConsultationMessage message);

    /**
     * 查询某预约的历史消息（分页）
     */
    List<ChatMessageVO> findByAppointmentId(@Param("appointmentId") Long appointmentId,
                                            @Param("currentUserId") Long currentUserId);
}