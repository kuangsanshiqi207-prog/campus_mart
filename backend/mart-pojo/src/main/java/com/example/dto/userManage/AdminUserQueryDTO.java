package com.example.dto.userManage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "管理端用户查询入参")
public class AdminUserQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词（用户名/昵称/手机号）")
    private String keyword;

    @Schema(description = "状态 normal/banned")
    private String status;

    @Schema(description = "是否校园认证 0否 1是")
    private Integer certified;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}