package com.example.dto.adminProduct;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "管理端商品查询入参")
public class AdminProductQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词（标题）")
    private String keyword;

    @Schema(description = "上架状态 on_sale/reserved/sold/offline")
    private String status;

    @Schema(description = "审核状态 pending/approved/rejected")
    private String auditStatus;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}