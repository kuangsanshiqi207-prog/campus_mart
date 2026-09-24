package com.example.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息信息")
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "消息类型 order/audit/report/system")
    private String type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "关联业务ID")
    private Long bizId;

    @Schema(description = "是否已读")
    private Integer isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}