package com.example.mapper.user;

import com.example.entity.Address;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressMapper {

    List<Address> listByUserId(Long userId);

    Address getById(Long id);

    void insert(Address address);

    void update(Address address);

    void clearDefault(Long userId);

    void deleteById(Long id);
}