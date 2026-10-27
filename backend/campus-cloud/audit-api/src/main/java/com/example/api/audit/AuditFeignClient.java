package com.example.api.audit;

import com.example.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 审核中心对外契约（唯一写方 = audit-service）。
 */
@FeignClient(name = "audit-service", path = "/internal/audit")
public interface AuditFeignClient {

    /** 触发商品异步自动审核（商品发布/修改提交后由 product-service 调用） */
    @PostMapping("/auto-audit-product/{productId}")
    Result<Void> autoAuditProduct(@PathVariable("productId") Long productId);
}
