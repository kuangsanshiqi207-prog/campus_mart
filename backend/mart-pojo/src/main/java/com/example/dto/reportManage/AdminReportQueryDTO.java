package com.example.dto.reportManage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "管理端举报查询入参")
public class AdminReportQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态 pending/valid/invalid")
    private String status;

    @Schema(description = "举报对象类型 product/user/order")
    private String targetType;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}