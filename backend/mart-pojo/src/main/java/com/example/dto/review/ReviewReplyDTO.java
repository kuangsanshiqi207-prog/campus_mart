package com.example.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "回复评价入参")
public class ReviewReplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "回复内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容最长 500 字符")
    private String content;
}