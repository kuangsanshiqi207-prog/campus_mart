package com.example.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "修改密码入参")
public class PasswordUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度 6-32")
    private String newPassword;
}