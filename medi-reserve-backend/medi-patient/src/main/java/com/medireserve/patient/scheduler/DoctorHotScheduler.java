package com.medireserve.patient.scheduler;

import com.medireserve.patient.service.EvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 热门医生排行榜定时刷新任务
 * 兜底方案：定时刷新缓存，防止因评价提交失败导致缓存过期
 */
@Slf4j
@Component
@EnableScheduling
public class DoctorHotScheduler {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 每30分钟刷新一次热门医生排行榜
     * cron 表达式：秒 分 时 日 月 周
     * 0 0/30 * * * ? = 每30分钟执行一次
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void refreshHotDoctorCache() {

        log.info("定时任务开始：刷新热门医生排行榜缓存");

        try {
            evaluationService.refreshHotDoctorCache();
            log.info("定时任务执行成功：热门医生缓存已刷新");
        } catch (Exception e) {
            log.error("定时任务执行失败：刷新热门医生缓存异常", e);
        }

    }

}