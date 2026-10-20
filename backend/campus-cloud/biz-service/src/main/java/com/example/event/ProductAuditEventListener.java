package com.example.event;

import cn.hutool.core.util.IdUtil;
import com.example.api.ai.AiAuditSupport;
import com.example.api.message.MessageFeignClient;
import com.example.constant.NotificationConstant;
import com.example.constant.ProductConstant;
import com.example.entity.AiAuditLog;
import com.example.entity.Product;
import com.example.mapper.ai.AiAuditLogMapper;
import com.example.mapper.product.ProductMapper;
import com.example.vo.ai.AiAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * 商品异步 AI 审核监听器。
 *
 * <p>事务提交后异步执行：AI 通过→自动上架，AI 驳回→自动下架并留原因，
 * 超时/异常→保持「审核中」等管理员人工审核。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAuditEventListener {

    private final AiAuditSupport aiAuditSupport;
    private final ProductMapper productMapper;
    private final AiAuditLogMapper aiAuditLogMapper;
    private final MessageFeignClient messageFeignClient;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductAudit(ProductAuditEvent event) {
        AiAuditVO vo = aiAuditSupport.callAudit(event.getDto());
        if (vo == null) {
            // AI 无结果（超时/异常）：保持 pending，等管理员审核
            log.warn("商品 {} AI 审核无结果，保持审核中状态", event.getProductId());
            return;
        }

        Product product = productMapper.getById(event.getProductId());
        if (product == null) {
            return;
        }

        if (Boolean.TRUE.equals(vo.getPassed())) {
            productMapper.updateAuditResult(event.getProductId(),
                    ProductConstant.AUDIT_APPROVED, null, ProductConstant.STATUS_ON_SALE);
            messageFeignClient.send(product.getSellerId(), NotificationConstant.TYPE_AUDIT,
                    "商品审核通过",
                    "您发布的商品【" + product.getTitle() + "】已审核通过。", event.getProductId());
        } else {
            productMapper.updateAuditResult(event.getProductId(),
                    ProductConstant.AUDIT_REJECTED, vo.getReason(), ProductConstant.STATUS_OFFLINE);
            messageFeignClient.send(product.getSellerId(), NotificationConstant.TYPE_AUDIT,
                    "商品审核未通过",
                    "您发布的商品【" + product.getTitle() + "】未通过审核，原因：" + vo.getReason(), event.getProductId());
        }

        saveAuditLog(event.getProductId(), vo);
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
