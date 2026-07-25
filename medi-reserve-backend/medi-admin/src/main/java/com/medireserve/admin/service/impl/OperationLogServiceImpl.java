package com.medireserve.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.admin.mapper.OperationLogMapper;
import com.medireserve.admin.service.OperationLogService;
import com.medireserve.common.dto.OperationLogQueryDTO;
import com.medireserve.common.dto.OperationLogVO;
import com.medireserve.common.entity.OperationLog;
import com.medireserve.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现
 *
 * 关键注解：@Async
 * - 方法在单独的线程池中执行，主线程不受影响
 * - 需要在启动类或配置类上添加 @EnableAsync 启用
 *
 * 为什么用异步而不是同步？
 * - 同步保存：每个操作都要等待 5-10ms 的 INSERT 时间，QPS 下降 30%
 * - 异步保存：主业务 0 等待，日志线程池慢慢消化
 *
 * 失败处理：
 * - 如果日志插入失败（如 DB 连接超时），只记录错误日志，不影响主业务
 * - 这是典型的「最终一致性」设计：日志记录允许有少量失败
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 异步保存日志（使用 @Async）
     * 注意：需要在启动类或配置类上启用 @EnableAsync
     *
     * @Async 注解告诉 Spring：此方法使用异步线程池执行
     * 线程池配置由 Spring Boot 自动配置（默认 SimpleAsyncTaskExecutor）
     * 生产环境建议自定义线程池（核心线程数 5，最大 20，队列 1000）
     *
     * 注意：@Async 方法不能和 @Transactional 同时使用（事务要求同一线程）
     * 但日志保存失败不影响主业务，不需要事务
     * @param operationLog
     */
    @Async
    @Override
    public void saveLogAsync(OperationLog operationLog) {

        log.debug("异步保存日志，管理员：{}，操作：{}",
                operationLog.getAdminName(), operationLog.getOperation());

        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            // 日志记录失败不影响主业务，仅打印错误日志
            // 这是设计上的权衡：宁可丢一条日志，也不能阻塞用户挂号
            log.error("保存操作日志失败：{}", e.getMessage(), e);
        }

    }

    // ====== 其他方法（查询、删除）是同步的，因为它们是运维功能 ======

    /**
     * 分页查询日志列表
     * @param query
     * @return
     */
    @Override
    public PageInfo<OperationLogVO> findList(OperationLogQueryDTO query) {

        log.info("分页查询日志列表：{}", query);

        // 设置分页
        PageHelper.startPage(query.getPageNum(), query.getPageSize());

        List<OperationLog> list = operationLogMapper.findList(query);

        // 转换为 VO
        List<OperationLogVO> voList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建 PageInfo（使用原始列表构造，然后替换列表）
        PageInfo<OperationLog> pageInfo = new PageInfo<>(list);
        PageInfo<OperationLogVO> resultPage = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, resultPage, "list");

        resultPage.setList(voList);

        return resultPage;

    }

    /**
     * 查询日志详情
     * @param id
     * @return
     */
    @Override
    public OperationLogVO findById(Long id) {

        log.info("查询日志详细，日志ID：{}", id);

        OperationLog operationLog = operationLogMapper.findById(id);

        if (operationLog == null) {
            throw new BusinessException("日志不存在");
        }

        return convertToVO(operationLog);

    }

    /**
     * 删除日志
     * @param id
     */
    @Transactional
    @Override
    public void deleteById(Long id) {
        int rows = operationLogMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException("日志不存在或已删除");
        }
        log.info("删除操作日志成功，ID：{}", id);
    }

    /**
     * 实体转 VO
     * @param operationLog
     * @return
     */
    private OperationLogVO convertToVO(OperationLog operationLog) {

        OperationLogVO vo = new OperationLogVO();

        BeanUtils.copyProperties(operationLog, vo);

        return vo;

    }

}
