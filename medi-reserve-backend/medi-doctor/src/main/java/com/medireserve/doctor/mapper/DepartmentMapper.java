package com.medireserve.doctor.mapper;

import com.medireserve.common.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    @Select("SELECT * FROM department ORDER BY sort_order ASC, id ASC")
    List<Department> findAll();

    @Select("SELECT * FROM department WHERE id = #{id}")
    Department findById(Long id);
}