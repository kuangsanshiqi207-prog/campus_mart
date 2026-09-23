package com.example.mapper.product;

import com.example.dto.product.ProductQueryDTO;
import com.example.entity.Product;
import com.example.vo.product.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product getById(Long id);

    List<ProductVO> listProducts(@Param("query") ProductQueryDTO query,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    Long countProducts(@Param("query") ProductQueryDTO query);

    List<ProductVO> listRecommend(@Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    List<ProductVO> listSimilar(@Param("categoryId") Long categoryId,
                                @Param("excludeId") Long excludeId,
                                @Param("limit") Integer limit);

    void incrementViewCount(Long id);

    void incrementFavoriteCount(Long id);

    void decrementFavoriteCount(Long id);

    Integer countOnSaleBySellerId(Long sellerId);
}