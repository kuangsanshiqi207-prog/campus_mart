package com.example.mapper.user;

import com.example.entity.UserCertification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCertificationMapper {

    List<UserCertification> listByUserId(Long userId);

    UserCertification getById(Long id);

    void insert(UserCertification certification);

    void updateStatus(@Param("id") Long id,
                      @Param("status") String status,
                      @Param("reason") String reason);
}