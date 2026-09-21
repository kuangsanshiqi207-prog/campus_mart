package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User getByUsername(String username);

    @Select("SELECT * FROM user WHERE phone = #{phone} AND deleted = 0")
    User getByPhone(String phone);

    @Select("SELECT * FROM user WHERE id = #{id} AND deleted = 0")
    User getById(Long id);

    @Insert("INSERT INTO user (id, username, password, nickname, avatar, phone, email, " +
            "role, status, certified, credit_score, create_time, update_time, deleted) " +
            "VALUES (#{id}, #{username}, #{password}, #{nickname}, #{avatar}, #{phone}, #{email}, " +
            "#{role}, #{status}, #{certified}, #{creditScore}, #{createTime}, #{updateTime}, 0)")
    void insert(User user);

    @Update("UPDATE user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    void updatePassword(@Param("id") Long id, @Param("password") String password);
}