package com.example.vo.userManage;

import com.example.vo.user.CertificationVO;
import com.example.vo.user.UserVO;
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
@Schema(description = "用户详情（含认证信息）")
public class UserDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户基本信息")
    private UserVO user;

    @Schema(description = "认证信息（可能为 null）")
    private CertificationVO certification;
}