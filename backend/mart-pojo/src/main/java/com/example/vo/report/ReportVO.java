package com.example.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "举报信息")
public class ReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "举报ID")
    private Long id;

    @Schema(description = "举报人ID")
    private Long reporterId;

    @Schema(description = "举报人昵称")
    private String reporterNickname;

    @Schema(description = "举报对象类型")
    private String targetType;

    @Schema(description = "举报对象ID")
    private Long targetId;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "证据图片URL列表")
    private List<String> images;

    @Schema(description = "状态 pending/valid/invalid")
    private String status;

    @Schema(description = "处理动作")
    private String action;

    @Schema(description = "处理说明")
    private String handleReason;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "举报时间")
    private LocalDateTime createTime;

    @Schema(description = "是否已申诉")
    private Boolean hasAppeal;
}