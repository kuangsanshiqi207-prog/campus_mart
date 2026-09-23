package com.example.mapper.product;

import com.example.entity.BrowseHistory;
import com.example.vo.product.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrowseHistoryMapper {

    BrowseHistory getByUserAndProduct(@Param("userId") Long userId,
                                      @Param("productId") Long productId);

    void insert(BrowseHistory history);

    void updateBrowseTime(@Param("userId") Long userId,
                          @Param("productId") Long productId);

    List<ProductVO> listByUserId(@Param("userId") Long userId,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    Long countByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}