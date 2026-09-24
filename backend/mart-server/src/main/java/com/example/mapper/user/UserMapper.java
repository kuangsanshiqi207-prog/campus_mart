package com.example.mapper.user;

import com.example.dto.userManage.AdminUserQueryDTO;
import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User getByUsername(String username);

    User getByPhone(String phone);

    User getById(Long id);

    void insert(User user);

    void updatePassword(@Param("id") Long id, @Param("password") String password);

    void updateProfile(User user);

    void updateCertified(@Param("id") Long id, @Param("certified") Integer certified);

    List<User> listUsers(@Param("query") AdminUserQueryDTO query,
                         @Param("offset") Integer offset,
                         @Param("limit") Integer limit);

    Long countUsers(@Param("query") AdminUserQueryDTO query);

    void updateStatus(@Param("id") Long id, @Param("status") String status);

    void updateCreditScore(@Param("id") Long id, @Param("score") Integer score);
}