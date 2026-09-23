package com.example.mapper.common;

import com.example.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileMapper {

    File getById(Long id);

    void insert(File file);

    void markUsed(@Param("id") Long id);
}