package com.example.mapper.stats;

import com.example.entity.CreditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CreditLogMapper {

    void insert(CreditLog log);

    List<CreditLog> listByUserId(@Param("userId") Long userId,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    Long countByUserId(Long userId);
}