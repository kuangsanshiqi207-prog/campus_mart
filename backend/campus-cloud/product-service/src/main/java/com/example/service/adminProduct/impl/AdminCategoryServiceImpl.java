package com.example.service.adminProduct.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.MessageConstant;
import com.example.dto.adminProduct.CategoryDTO;
import com.example.entity.Category;
import com.example.exception.BaseException;
import com.example.mapper.product.CategoryMapper;
import com.example.service.adminProduct.AdminCategoryService;
import com.example.vo.product.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> listCategories() {
        return categoryMapper.listAll().stream()
                .map(this::toCategoryVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createCategory(CategoryDTO dto) {
        Category category = Category.builder()
                .id(IdUtil.getSnowflakeNextId())
                .name(dto.getName())
                .parentId(dto.getParentId() != null ? dto.getParentId() : 0L)
                .sort(dto.getSort() != null ? dto.getSort() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional
    public void updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryMapper.getById(id);
        if (category == null) {
            throw new BaseException("分类不存在");
        }

        Category update = Category.builder()
                .id(id)
                .name(dto.getName())
                .parentId(dto.getParentId())
                .sort(dto.getSort())
                .status(dto.getStatus())
                .build();

        categoryMapper.update(update);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryMapper.getById(id);
        if (category == null) {
            throw new BaseException("分类不存在");
        }

        // 检查分类下是否有商品
        Integer count = categoryMapper.countProductsByCategory(id);
        if (count != null && count > 0) {
            throw new BaseException("该分类下有 " + count + " 个商品，无法删除");
        }

        categoryMapper.deleteById(id);
    }

    private CategoryVO toCategoryVO(Category category) {
        return CategoryVO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParentId())
                .sort(category.getSort())
                .build();
    }
}