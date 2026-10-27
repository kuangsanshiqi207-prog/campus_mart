package com.example.event;

import com.example.api.audit.AuditFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 商品异步 AI 审核监听器。
 *
 * <p>事务提交后异步调用 audit-service 触发自动审核（审核逻辑已收口到 audit-service）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAuditEventListener {

    private final AuditFeignClient auditFeignClient;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductAudit(ProductAuditEvent event) {
        try {
            auditFeignClient.autoAuditProduct(event.getProductId());
        } catch (Exception e) {
            log.warn("触发商品 {} 自动审核失败：{}", event.getProductId(), e.getMessage());
        }
    }
}
