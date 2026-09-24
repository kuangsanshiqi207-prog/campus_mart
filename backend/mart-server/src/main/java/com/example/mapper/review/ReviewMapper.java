package com.example.mapper.review;

import com.example.entity.Review;
import com.example.vo.review.ReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    Review getById(Long id);

    Review getByOrderAndFrom(@Param("orderId") Long orderId,
                             @Param("fromUserId") Long fromUserId);

    void insert(Review review);

    void updateReply(@Param("id") Long id,
                     @Param("reply") String reply);

    ReviewVO getReviewVOById(Long id);

    List<ReviewVO> listByProduct(@Param("productId") Long productId,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    Long countByProduct(Long productId);

    List<ReviewVO> listByFromUser(@Param("userId") Long userId,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    Long countByFromUser(Long userId);

    List<ReviewVO> listByToUser(@Param("userId") Long userId,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    Long countByToUser(Long userId);
}