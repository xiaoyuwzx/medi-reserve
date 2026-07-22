package com.medireserve.admin.service;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.OperationLogQueryDTO;
import com.medireserve.common.dto.OperationLogVO;
import com.medireserve.common.entity.OperationLog;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 异步保存日志（由 AOP 调用）
     * @param log
     */
    void saveLogAsync(OperationLog log);

    /**
     * 分页查询日志列表
     * @param query
     * @return
     */
    PageInfo<OperationLogVO> findList(OperationLogQueryDTO query);

    /**
     * 查询日志详情
     * @param id
     * @return
     */
    OperationLogVO findById(Long id);

    /**
     * 删除日志
     * @param id
     */
    void deleteById(Long id);

}
