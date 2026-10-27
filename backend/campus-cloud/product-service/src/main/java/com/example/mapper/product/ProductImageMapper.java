package com.example.mapper.product;

import com.example.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductImageMapper {

    List<ProductImage> listByProductId(Long productId);

    void batchInsert(List<ProductImage> images);

    void deleteByProductId(Long productId);
}