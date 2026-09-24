package com.example.service.review;

import com.example.dto.review.ReviewCreateDTO;
import com.example.dto.review.ReviewReplyDTO;
import com.example.result.PageResult;
import com.example.vo.review.ReviewVO;

public interface ReviewService {

    Long createReview(ReviewCreateDTO dto);

    void replyReview(Long id, ReviewReplyDTO dto);

    PageResult listByProduct(Long productId, Integer pageNum, Integer pageSize);

    PageResult listMyReviews(Integer pageNum, Integer pageSize);

    PageResult listReceivedReviews(Integer pageNum, Integer pageSize);

    ReviewVO getReviewDetail(Long id);
}