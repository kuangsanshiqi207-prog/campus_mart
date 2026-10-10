package com.example.event;

import com.example.api.ai.AiFeignClient;
import com.example.api.message.MessageFeignClient;
import com.example.constant.NotificationConstant;
import com.example.constant.ProductConstant;
import com.example.dto.ai.AiAuditDTO;
import com.example.entity.Product;
import com.example.mapper.product.ProductMapper;
import com.example.result.Result;
import com.example.vo.ai.AiAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

    private final AiFeignClient aiFeignClient;
    private final ProductMapper productMapper;
    private final MessageFeignClient messageFeignClient;

    @Async("aiAuditExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductAudit(ProductAuditEvent event) {
        AiAuditVO vo = callAi(event.getDto());
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
            productMapper.updateAuditStatus(event.getProductId(), ProductConstant.AUDIT_APPROVED, null);
            productMapper.updateStatus(event.getProductId(), ProductConstant.STATUS_ON_SALE);
            messageFeignClient.send(product.getSellerId(), NotificationConstant.TYPE_AUDIT,
                    "商品审核通过",
                    "您发布的商品【" + product.getTitle() + "】已审核通过。", event.getProductId());
        } else {
            productMapper.updateAuditStatus(event.getProductId(), ProductConstant.AUDIT_REJECTED, vo.getReason());
            productMapper.updateStatus(event.getProductId(), ProductConstant.STATUS_OFFLINE);
            messageFeignClient.send(product.getSellerId(), NotificationConstant.TYPE_AUDIT,
                    "商品审核未通过",
                    "您发布的商品【" + product.getTitle() + "】未通过审核，原因：" + vo.getReason(), event.getProductId());
        }
    }

    private AiAuditVO callAi(AiAuditDTO dto) {
        try {
            Result<AiAuditVO> result = aiFeignClient.audit(dto);
            if (result != null && result.getCode() != null && result.getCode() == 1 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("AI 审核调用失败：{}", e.getMessage());
        }
        return null;
    }
}
