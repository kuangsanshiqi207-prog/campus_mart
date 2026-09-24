package com.example.mapper.product;

import com.example.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> listEnabled();

    Category getById(Long id);

    List<Category> listAll();

    void insert(Category category);

    void update(Category category);

    void deleteById(Long id);

    Integer countProductsByCategory(Long categoryId);
}