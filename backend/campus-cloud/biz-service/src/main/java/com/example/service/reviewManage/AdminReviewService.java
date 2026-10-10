package com.example.service.reviewManage;

import com.example.dto.reviewManage.AdminReviewQueryDTO;
import com.example.result.PageResult;
import com.example.vo.ai.AiAuditVO;

public interface AdminReviewService {

    PageResult listReviews(AdminReviewQueryDTO query);

    AiAuditVO aiAudit(Long id);

    void deleteReview(Long id);
}