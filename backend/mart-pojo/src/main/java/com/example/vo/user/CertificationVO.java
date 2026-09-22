package com.example.vo.user;

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
@Schema(description = "校园认证信息")
public class CertificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "认证记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "学校")
    private String school;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "学生证图片URL")
    private String cardImage;

    @Schema(description = "审核状态 pending/approved/rejected")
    private String status;

    @Schema(description = "审核拒绝原因")
    private String reason;

    @Schema(description = "提交时间")
    private LocalDateTime createTime;
}