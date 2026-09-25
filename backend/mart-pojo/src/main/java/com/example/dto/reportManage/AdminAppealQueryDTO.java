package com.example.dto.reportManage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "管理端申诉查询入参")
public class AdminAppealQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态 pending/approved/rejected")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}