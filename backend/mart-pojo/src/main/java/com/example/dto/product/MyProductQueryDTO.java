package com.example.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "我的商品查询入参")
public class MyProductQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态筛选 on_sale/reserved/sold/offline")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}