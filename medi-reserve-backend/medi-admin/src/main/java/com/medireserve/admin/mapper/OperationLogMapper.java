package com.medireserve.admin.mapper;

import com.medireserve.common.dto.OperationLogQueryDTO;
import com.medireserve.common.entity.OperationLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作日志 Mapper 接口
 */
@Mapper
public interface OperationLogMapper {

    /**
     * 插入一条日志（由 AOP 调用）
     * @param log
     * @return
     */
    int insert(OperationLog log);

    /**
     * 根据条件查询日志列表（分页）
     * @param query
     * @return
     */
    List<OperationLog> findList(@Param("q") OperationLogQueryDTO query);

    /**
     * 根据 ID 查询日志
     * @param id
     * @return
     */
    @Select("SELECT * FROM operation_log WHERE id = #{id}")
    OperationLog findById(@Param("id") Long id);

    /**
     * 统计符合条件的总记录数
     * @param query
     * @return
     */
    long count(@Param("q") OperationLogQueryDTO query);

    /**
     * 删除单条日志
     * @param id
     * @return
     */
    @Delete("DELETE FROM operation_log WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

}
