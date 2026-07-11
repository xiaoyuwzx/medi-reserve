package com.medireserve.doctor.mapper;

import com.medireserve.common.entity.Title;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TitleMapper {

    @Select("SELECT * FROM title ORDER BY sort_order ASC, id ASC")
    List<Title> findAll();

    @Select("SELECT * FROM title WHERE id = #{id}")
    Title findById(Long id);
}