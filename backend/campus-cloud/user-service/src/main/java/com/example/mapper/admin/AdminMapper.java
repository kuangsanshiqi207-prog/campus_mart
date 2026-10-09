package com.example.mapper.admin;

import com.example.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    Admin getByUsername(String username);

    Admin getById(Long id);
}