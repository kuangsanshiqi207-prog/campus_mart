package com.example.controller.internal;

import com.example.api.audit.AuditFeignClient;
import com.example.result.Result;
import com.example.service.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/audit")
@RequiredArgsConstructor
public class AuditInternalController implements AuditFeignClient {

    private final AuditService auditService;

    @Override
    public Result<Void> autoAuditProduct(Long productId) {
        auditService.autoAuditProduct(productId);
        return Result.success();
    }
}
