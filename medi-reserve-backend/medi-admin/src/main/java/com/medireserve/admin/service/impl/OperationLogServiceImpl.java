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
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 异步保存日志（使用 @Async）
     * 注意：需要在启动类或配置类上启用 @EnableAsync
     * @param operationLog
     */
    @Async
    @Override
    public void saveLogAsync(OperationLog operationLog) {

        log.info("异步保存日志，日志ID：{}", operationLog.getId());

        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            // 日志记录失败不影响主业务，仅打印错误日志
            log.error("保存操作日志失败：{}", e.getMessage(), e);
        }

    }

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
