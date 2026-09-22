package com.example.mapper.user;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User getByUsername(String username);

    User getByPhone(String phone);

    User getById(Long id);

    void insert(User user);

    void updatePassword(@Param("id") Long id, @Param("password") String password);

    void updateProfile(User user);

    void updateCertified(@Param("id") Long id, @Param("certified") Integer certified);
}