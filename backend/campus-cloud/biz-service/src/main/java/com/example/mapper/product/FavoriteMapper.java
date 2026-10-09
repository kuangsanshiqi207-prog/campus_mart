package com.example.mapper.product;

import com.example.entity.Favorite;
import com.example.vo.product.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    Favorite getByUserAndProduct(@Param("userId") Long userId,
                                 @Param("productId") Long productId);

    void insert(Favorite favorite);

    void deleteByUserAndProduct(@Param("userId") Long userId,
                                @Param("productId") Long productId);

    List<ProductVO> listByUserId(@Param("userId") Long userId,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    Long countByUserId(Long userId);
}