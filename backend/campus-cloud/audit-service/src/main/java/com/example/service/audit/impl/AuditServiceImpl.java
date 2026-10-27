package com.example.service.audit.impl;

import cn.hutool.core.util.IdUtil;
import com.example.api.ai.AiAuditSupport;
import com.example.api.message.MessageFeignClient;
import com.example.api.product.ProductFeignClient;
import com.example.api.user.UserFeignClient;
import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.constant.ProductConstant;
import com.example.constant.UserConstant;
import com.example.dto.adminProduct.ProductAuditDTO;
import com.example.dto.ai.AiAuditDTO;
import com.example.dto.userManage.CertificationAuditDTO;
import com.example.entity.AiAuditLog;
import com.example.entity.Product;
import com.example.entity.Review;
import com.example.entity.UserCertification;
import com.example.exception.BaseException;
import com.example.mapper.ai.AiAuditLogMapper;
import com.example.result.Result;
import com.example.service.audit.AuditService;
import com.example.vo.ai.AiAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;
    private final MessageFeignClient messageFeignClient;
    private final AiAuditSupport aiAuditSupport;
    private final AiAuditLogMapper aiAuditLogMapper;

    // ==================== 商品人工审核 ====================

    @Override
    public void auditProduct(Long id, ProductAuditDTO dto) {
        Product product = getProduct(id);

        if (!ProductConstant.AUDIT_PENDING.equals(product.getAuditStatus())) {
            throw new BaseException("该商品已审核过");
        }
        if (!"pass".equals(dto.getResult()) && !"reject".equals(dto.getResult())) {
            throw new BaseException("审核结果不合法");
        }

        if ("pass".equals(dto.getResult())) {
            productFeignClient.updateAuditResult(id, ProductConstant.AUDIT_APPROVED, null, ProductConstant.STATUS_ON_SALE);
            messageFeignClient.send(product.getSellerId(), NotificationConstant.TYPE_AUDIT,
                    "商品审核通过", "您发布的商品【" + product.getTitle() + "】已审核通过。", id);
        } else {
            productFeignClient.updateAuditResult(id, ProductConstant.AUDIT_REJECTED, dto.getReason(), ProductConstant.STATUS_OFFLINE);
            messageFeignClient.send(product.getSellerId(), NotificationConstant.TYPE_AUDIT,
                    "商品审核未通过", "您发布的商品【" + product.getTitle() + "】未通过审核，原因：" + dto.getReason(), id);
        }
    }

    // ==================== 商品 AI 辅助 ====================

    @Override
    public AiAuditVO aiAuditProduct(Long id) {
        Product product = getProduct(id);
        return aiAuditSupport.callAudit(buildProductAiAuditDTO(product, listImageUrls(id)));
    }

    // ==================== 删除违规评论 ====================

    @Override
    public void deleteReview(Long id) {
        Review review = getReview(id);
        productFeignClient.deleteReview(id);
        messageFeignClient.send(review.getFromUserId(), NotificationConstant.TYPE_SYSTEM,
                "评价已被删除", "您发表的评价已被管理员删除。", id);
    }

    // ==================== 评论 AI 辅助 ====================

    @Override
    public AiAuditVO aiAuditReview(Long id) {
        Review review = getReview(id);

        AiAuditDTO dto = new AiAuditDTO();
        dto.setAuditType("review");
        Map<String, String> textFields = new HashMap<>();
        aiAuditSupport.putIfPresent(textFields, "content", review.getContent());
        if (review.getScore() != null) {
            textFields.put("score", review.getScore().toString());
        }
        dto.setTextFields(textFields);
        dto.setImageUrls(aiAuditSupport.splitImages(review.getImages()));

        return aiAuditSupport.callAudit(dto);
    }

    // ==================== 认证审核 ====================

    @Override
    public void auditCertification(Long id, CertificationAuditDTO dto) {
        UserCertification cert = getCertification(id);

        if (!UserConstant.CERT_STATUS_PENDING.equals(cert.getStatus())) {
            throw new BaseException("该认证已审核过");
        }
        if (!UserConstant.CERT_STATUS_APPROVED.equals(dto.getStatus())
                && !UserConstant.CERT_STATUS_REJECTED.equals(dto.getStatus())) {
            throw new BaseException("审核结果不合法");
        }

        userFeignClient.auditCertification(id, dto.getStatus(), dto.getReason());

        if (UserConstant.CERT_STATUS_APPROVED.equals(dto.getStatus())) {
            messageFeignClient.send(cert.getUserId(), NotificationConstant.TYPE_AUDIT,
                    NotificationConstant.AUDIT_CERT_APPROVED_TITLE,
                    NotificationConstant.AUDIT_CERT_APPROVED_CONTENT, id);
        } else {
            messageFeignClient.send(cert.getUserId(), NotificationConstant.TYPE_AUDIT,
                    NotificationConstant.AUDIT_CERT_REJECTED_TITLE,
                    String.format(NotificationConstant.AUDIT_CERT_REJECTED_CONTENT, dto.getReason()), id);
        }
    }

    // ==================== 认证 AI 辅助 ====================

    @Override
    public AiAuditVO aiAuditCertification(Long id) {
        UserCertification cert = getCertification(id);

        AiAuditDTO dto = new AiAuditDTO();
        dto.setAuditType("certification");
        Map<String, String> textFields = new HashMap<>();
        aiAuditSupport.putIfPresent(textFields, "school", cert.getSchool());
        aiAuditSupport.putIfPresent(textFields, "studentNo", cert.getStudentNo());
        aiAuditSupport.putIfPresent(textFields, "realName", cert.getRealName());
        dto.setTextFields(textFields);

        List<String> imageUrls = new ArrayList<>();
        if (cert.getCardImage() != null && !cert.getCardImage().isBlank()) {
            imageUrls.add(cert.getCardImage());
        }
        dto.setImageUrls(imageUrls);

        return aiAuditSupport.callAudit(dto);
    }

    // ==================== 商品自动审核（异步） ====================

    @Async("asyncExecutor")
    @Override
    public void autoAuditProduct(Long productId) {
        Product product = getProduct(productId);
        if (product == null) {
            return;
        }

        AiAuditVO vo = aiAuditSupport.callAudit(buildProductAiAuditDTO(product, listImageUrls(productId)));
        if (vo == null) {
            log.warn("商品 {} AI 审核无结果，保持审核中状态", productId);
            return;
        }

        if (Boolean.TRUE.equals(vo.getPassed())) {
            productFeignClient.updateAuditResult(productId,
                    ProductConstant.AUDIT_APPROVED, null, ProductConstant.STATUS_ON_SALE);
            messageFeignClient.send(product.getSellerId(), NotificationConstant.TYPE_AUDIT,
                    "商品审核通过", "您发布的商品【" + product.getTitle() + "】已审核通过。", productId);
        } else {
            productFeignClient.updateAuditResult(productId,
                    ProductConstant.AUDIT_REJECTED, vo.getReason(), ProductConstant.STATUS_OFFLINE);
            messageFeignClient.send(product.getSellerId(), NotificationConstant.TYPE_AUDIT,
                    "商品审核未通过", "您发布的商品【" + product.getTitle() + "】未通过审核，原因：" + vo.getReason(), productId);
        }

        saveAuditLog(productId, vo);
    }

    // ==================== 私有方法 ====================

    private Product getProduct(Long id) {
        Result<Product> r = productFeignClient.getById(id);
        if (r == null || r.getCode() != 1 || r.getData() == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        return r.getData();
    }

    private List<String> listImageUrls(Long productId) {
        Result<List<String>> r = productFeignClient.listImageUrls(productId);
        if (r == null || r.getCode() != 1 || r.getData() == null) {
            return List.of();
        }
        return r.getData();
    }

    private Review getReview(Long id) {
        Result<Review> r = productFeignClient.getReviewById(id);
        if (r == null || r.getCode() != 1 || r.getData() == null) {
            throw new BaseException(MessageConstant.REVIEW_NOT_FOUND);
        }
        return r.getData();
    }

    private UserCertification getCertification(Long id) {
        Result<UserCertification> r = userFeignClient.getCertificationById(id);
        if (r == null || r.getCode() != 1 || r.getData() == null) {
            throw new BaseException(MessageConstant.CERTIFICATION_NOT_FOUND);
        }
        return r.getData();
    }

    private AiAuditDTO buildProductAiAuditDTO(Product product, List<String> imageUrls) {
        AiAuditDTO dto = new AiAuditDTO();
        dto.setAuditType("product");

        Map<String, String> textFields = new HashMap<>();
        aiAuditSupport.putIfPresent(textFields, "title", product.getTitle());
        aiAuditSupport.putIfPresent(textFields, "description", product.getDescription());
        aiAuditSupport.putIfPresent(textFields, "price", product.getPrice() == null ? null : product.getPrice().toPlainString());
        aiAuditSupport.putIfPresent(textFields, "quality", product.getQuality());
        aiAuditSupport.putIfPresent(textFields, "tradeType", product.getTradeType());
        aiAuditSupport.putIfPresent(textFields, "tradePlace", product.getTradePlace());
        dto.setTextFields(textFields);

        dto.setImageUrls(imageUrls == null ? List.of() : imageUrls);
        return dto;
    }

    private void saveAuditLog(Long productId, AiAuditVO vo) {
        try {
            aiAuditLogMapper.insert(AiAuditLog.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .auditType("product")
                    .bizId(productId)
                    .passed(vo.getPassed())
                    .reason(vo.getReason())
                    .riskCategory(vo.getRiskCategory())
                    .riskLevel(vo.getRiskLevel())
                    .suggestedAction(vo.getSuggestedAction())
                    .createTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("AI 审核日志写入失败：{}", e.getMessage());
        }
    }
}
