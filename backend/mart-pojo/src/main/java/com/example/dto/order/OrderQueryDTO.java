package com.example.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "订单查询入参")
public class OrderQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "视角 buy/sell", example = "buy")
    private String type;

    @Schema(description = "状态筛选")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}