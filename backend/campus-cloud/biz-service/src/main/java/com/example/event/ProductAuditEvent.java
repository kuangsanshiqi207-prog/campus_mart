package com.example.event;

import com.example.dto.ai.AiAuditDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 商品发布/修改后触发的异步 AI 审核事件。
 */
@Data
@AllArgsConstructor
public class ProductAuditEvent {

    private Long productId;

    private AiAuditDTO dto;
}
