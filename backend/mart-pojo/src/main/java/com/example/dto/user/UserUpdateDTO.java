package com.example.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "修改个人资料入参")
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称", example = "小明")
    @Size(max = 50, message = "昵称最长 50 字符")
    private String nickname;

    @Schema(description = "头像URL", example = "https://oss.xxx.com/avatar.jpg")
    @Size(max = 255, message = "头像URL过长")
    private String avatar;

    @Schema(description = "邮箱", example = "test@example.com")
    @Email(message = "邮箱格式错误")
    @Size(max = 100, message = "邮箱最长 100 字符")
    private String email;
}