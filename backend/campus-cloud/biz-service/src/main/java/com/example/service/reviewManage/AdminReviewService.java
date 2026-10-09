package com.example.service.reviewManage;

import com.example.dto.reviewManage.AdminReviewQueryDTO;
import com.example.result.PageResult;

public interface AdminReviewService {

    PageResult listReviews(AdminReviewQueryDTO query);

    void deleteReview(Long id);
}