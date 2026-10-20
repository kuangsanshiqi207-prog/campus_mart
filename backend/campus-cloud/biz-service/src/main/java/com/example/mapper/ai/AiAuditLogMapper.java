package com.example.mapper.ai;

import com.example.entity.AiAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiAuditLogMapper {

    void insert(AiAuditLog log);
}
