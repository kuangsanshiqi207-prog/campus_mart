package com.example.api.ai;

import com.example.dto.ai.AiAuditDTO;
import com.example.result.Result;
import com.example.vo.ai.AiAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 审核通用支撑：封装 Feign 调用与请求体组装，供各服务复用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAuditSupport {

    private final AiFeignClient aiFeignClient;

    /** 调用 AI 审核，超时/失败返回 null（由调用方降级为人工审核）。 */
    public AiAuditVO callAudit(AiAuditDTO dto) {
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

    /** 值非空才放入 text_fields。 */
    public void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    /** 逗号分隔的图片串 → 列表。 */
    public List<String> splitImages(String images) {
        if (images == null || images.isBlank()) {
            return List.of();
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
