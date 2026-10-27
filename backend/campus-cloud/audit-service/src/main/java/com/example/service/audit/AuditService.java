package com.example.service.audit;

import com.example.dto.adminProduct.ProductAuditDTO;
import com.example.dto.userManage.CertificationAuditDTO;
import com.example.vo.ai.AiAuditVO;

public interface AuditService {

    void auditProduct(Long id, ProductAuditDTO dto);

    AiAuditVO aiAuditProduct(Long id);

    void deleteReview(Long id);

    AiAuditVO aiAuditReview(Long id);

    void auditCertification(Long id, CertificationAuditDTO dto);

    AiAuditVO aiAuditCertification(Long id);

    void autoAuditProduct(Long productId);
}
