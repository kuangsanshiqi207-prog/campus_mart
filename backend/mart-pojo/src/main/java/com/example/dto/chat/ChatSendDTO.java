package com.example.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "发送消息入参")
public class ChatSendDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容最长 2000 字符")
    private String content;

    @Schema(description = "消息类型 text/image/product/order")
    private String type = "text";
}