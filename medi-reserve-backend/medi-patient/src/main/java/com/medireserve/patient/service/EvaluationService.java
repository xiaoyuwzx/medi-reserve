package com.medireserve.patient.service;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.DoctorHotVO;
import com.medireserve.common.dto.EvaluationCreateDTO;
import com.medireserve.common.dto.EvaluationListVO;
import com.medireserve.common.dto.MyEvaluationVO;
import com.medireserve.common.entity.Evaluation;

import java.util.List;

/**
 * 评价服务接口
 * 定义评价相关的所有业务操作
 */
public interface EvaluationService {

    /**
     * 创建评价
     * 包含完整的业务校验：预约归属、状态、日期、重复评价等
     * @param patientId 当前登录患者ID
     * @param createDTO 评价创建参数
     * @return 创建成功的评价实体
     */
    Evaluation createEvaluation(Long patientId, EvaluationCreateDTO createDTO);

    /**
     * 查询我的评价列表（分页）
     * @param patientId 当前登录患者ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页评价列表
     */
    //PageInfo<MyEvaluationVO> getMyEvaluations(Long patientId, int page, int size);

    /**
     * 查询医生的评价列表（公开访问，分页）
     * @param doctorId 医生ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页评价列表
     */
    //PageInfo<EvaluationListVO> getDoctorEvaluations(Long doctorId, int page, int size);

    /**
     * 删除评价（软删除）
     * 只能删除自己发布的、状态为已发布的评价
     * @param evaluationId 评价ID
     * @param patientId 当前登录患者ID
     */
    //void deleteEvaluation(Long evaluationId, Long patientId);

    /**
     * 获取热门医生排行榜
     * 优先从 Redis 缓存读取，缓存不存在时查询数据库
     * @return 热门医生列表（最多10条）
     */
    //List<DoctorHotVO> getHotDoctors();

    /**
     * 刷新热门医生缓存（评价提交时调用 + 定时任务调用）
     * 重新计算排行榜并写入 Redis
     */
    void refreshHotDoctorCache();

}