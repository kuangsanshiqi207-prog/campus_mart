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
@Schema(description = "申诉信息")
public class AppealVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "申诉ID")
    private Long id;

    @Schema(description = "举报ID")
    private Long reportId;

    @Schema(description = "申诉人ID")
    private Long userId;

    @Schema(description = "申诉人昵称")
    private String userNickname;

    @Schema(description = "申诉理由")
    private String reason;

    @Schema(description = "证据图片URL列表")
    private List<String> images;

    @Schema(description = "状态 pending/approved/rejected")
    private String status;

    @Schema(description = "处理说明")
    private String handleReason;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "申诉时间")
    private LocalDateTime createTime;
}