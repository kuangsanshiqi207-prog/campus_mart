package com.example.mapper.product;

import com.example.dto.adminProduct.AdminProductQueryDTO;
import com.example.dto.product.ProductQueryDTO;
import com.example.entity.Product;
import com.example.vo.product.ProductVO;
import com.example.vo.product.UserProductStatsVO;
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

    void insert(Product product);

    void update(Product product);

    void updateStatus(@Param("id") Long id, @Param("status") String status);

    void updateAuditStatus(@Param("id") Long id,
                           @Param("auditStatus") String auditStatus,
                           @Param("auditReason") String auditReason);

    void deleteById(Long id);

    List<ProductVO> listMine(@Param("sellerId") Long sellerId,
                             @Param("status") String status,
                             @Param("offset") Integer offset,
                             @Param("limit") Integer limit);

    Long countMine(@Param("sellerId") Long sellerId,
                   @Param("status") String status);

    UserProductStatsVO getMyStats(Long sellerId);

    List<Product> listAdmin(@Param("query") AdminProductQueryDTO query,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);

    Long countAdmin(@Param("query") AdminProductQueryDTO query);

    void updateAudit(@Param("id") Long id,
                     @Param("auditStatus") String auditStatus,
                     @Param("auditReason") String auditReason);

    void forceOffline(@Param("id") Long id);
}