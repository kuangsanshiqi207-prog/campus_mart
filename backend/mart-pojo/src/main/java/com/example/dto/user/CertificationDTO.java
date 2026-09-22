package com.example.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "提交校园认证入参")
public class CertificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "学校", example = "某某大学", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学校不能为空")
    @Size(max = 100, message = "学校名称最长 100 字符")
    private String school;

    @Schema(description = "学号", example = "20210001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学号不能为空")
    @Size(max = 50, message = "学号最长 50 字符")
    private String studentNo;

    @Schema(description = "真实姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名最长 50 字符")
    private String realName;

    @Schema(description = "学生证图片URL")
    @Size(max = 500, message = "图片URL过长")
    private String cardImage;
}