package com.medireserve.websocket.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.ChatMessageVO;
import com.medireserve.common.dto.ConsultationRoomVO;
import com.medireserve.common.result.Result;
import com.medireserve.websocket.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 问诊辅助接口（HTTP）
 *
 * 注意：这些接口走的是 HTTP，不是 WebSocket
 * 用于进入问诊室前的信息查询和历史记录加载
 */
@Slf4j
@RestController
@RequestMapping("/consultation")
@Tag(name = "WebSocket 在线问诊", description = "问诊室信息、历史记录、结束问诊")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    /**
     * 获取问诊室信息（进入页面时调用）
     */
    @GetMapping("/room/{appointmentId}")
    @RequireRole({RoleConstant.PATIENT, RoleConstant.DOCTOR})
    @Operation(summary = "获取问诊室信息", description = "返回患者/医生信息、在线人数等")
    public Result<ConsultationRoomVO> getRoomInfo(
            @PathVariable Long appointmentId,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") String role) {

        log.info("获取问诊室信息，预约ID：{}，用户：{}", appointmentId, userId);

        // 内部调用 checkConsultationAccess 进行权限校验
        ConsultationRoomVO roomVO = consultationService.getRoomInfo(appointmentId, userId, role);

        return Result.success(roomVO);
    }

    /**
     * 获取聊天历史记录（分页）
     *
     * 加载策略：按时间正序排列（最新消息在最后）
     * 前端滚动到顶部时加载更早的消息
     */
    @GetMapping("/history/{appointmentId}")
    @RequireRole({RoleConstant.PATIENT, RoleConstant.DOCTOR})
    @Operation(summary = "获取聊天历史", description = "分页加载历史聊天记录")
    public Result<PageInfo<ChatMessageVO>> getHistory(
            @PathVariable Long appointmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") String role) {

        log.info("获取聊天历史，预约ID：{}，页码：{}", appointmentId, page);

        PageInfo<ChatMessageVO> pageInfo = consultationService.getHistory(appointmentId, userId, role, page, size);

        return Result.success(pageInfo);
    }

    /**
     * 结束问诊（患者或医生均可发起）
     *
     * 执行逻辑：
     * 1. 校验权限
     * 2. 更新预约状态为「已完成」（2）
     * 3. 清理 Redis 房间成员
     *
     * 注意：结束问诊后，WebSocket 连接不会自动断开
     * 前端需要监听状态变化，引导用户退出聊天室
     */
    @PostMapping("/end/{appointmentId}")
    @RequireRole({RoleConstant.PATIENT, RoleConstant.DOCTOR})
    @Operation(summary = "结束问诊", description = "患者或医生主动结束问诊，修改预约状态为已完成")
    public Result<String> endConsultation(
            @PathVariable Long appointmentId,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") String role) {

        log.info("结束问诊，预约ID：{}，操作人：{}", appointmentId, userId);

        consultationService.endConsultation(appointmentId, userId, role);

        return Result.success(MessageConstant.CONSULTATION_ENDED);
    }
}