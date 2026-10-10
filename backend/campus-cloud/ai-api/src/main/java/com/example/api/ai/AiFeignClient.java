package com.example.api.ai;

import com.example.dto.ai.AiAuditDTO;
import com.example.dto.ai.AiChatDTO;
import com.example.result.Result;
import com.example.vo.ai.AiAuditVO;
import com.example.vo.ai.AiChatVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI 服务 Feign 客户端（服务名 ai-service，由 AI 服务注册到 Nacos）。
 */
@FeignClient(name = "ai-service", path = "/api/v1")
public interface AiFeignClient {

    /**
     * AI 审核（商品/评论/举报/实名认证），auditType 取值见 AiAuditDTO。
     */
    @PostMapping("/audit")
    Result<AiAuditVO> audit(@RequestBody AiAuditDTO dto);

    /**
     * AI 客服，同步返回完整回答。
     * 流式输出走 SSE（/customer-service/chat/stream），Feign 不支持，需直连。
     */
    @PostMapping("/customer-service/chat")
    Result<AiChatVO> chat(@RequestBody AiChatDTO dto);
}
