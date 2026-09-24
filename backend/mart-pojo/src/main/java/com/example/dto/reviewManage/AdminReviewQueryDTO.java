package com.example.dto.reviewManage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "管理端评价查询入参")
public class AdminReviewQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词，模糊匹配评价内容")
    private String keyword;

    @Schema(description = "评分 1-5")
    private Integer score;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}