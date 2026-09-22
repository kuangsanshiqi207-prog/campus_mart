package com.example.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "地址入参")
public class AddressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "收货人", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人最长 50 字符")
    private String receiver;

    @Schema(description = "手机号", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Schema(description = "地区", example = "某某大学 3 栋")
    @Size(max = 100, message = "地区最长 100 字符")
    private String region;

    @Schema(description = "详细地址", example = "502 室", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址最长 255 字符")
    private String detail;

    @Schema(description = "是否默认 0否 1是", example = "0")
    private Integer isDefault;
}