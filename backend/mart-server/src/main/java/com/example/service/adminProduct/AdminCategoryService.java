package com.example.service.adminProduct;

import com.example.dto.adminProduct.CategoryDTO;
import com.example.vo.product.CategoryVO;

import java.util.List;

public interface AdminCategoryService {

    List<CategoryVO> listCategories();

    Long createCategory(CategoryDTO dto);

    void updateCategory(Long id, CategoryDTO dto);

    void deleteCategory(Long id);
}