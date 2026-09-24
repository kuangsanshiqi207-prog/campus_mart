package com.example.dto.adminProduct;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "下架入参")
public class OfflineDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "下架原因")
    private String reason;
}