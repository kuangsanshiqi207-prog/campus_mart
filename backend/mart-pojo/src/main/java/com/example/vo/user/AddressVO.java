package com.example.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收货地址信息")
public class AddressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "收货人")
    private String receiver;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "详细地址")
    private String detail;

    @Schema(description = "是否默认 0否 1是")
    private Integer isDefault;
}