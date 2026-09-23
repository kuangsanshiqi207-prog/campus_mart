package com.example.vo.common;

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
@Schema(description = "文件上传返回")
public class FileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件ID，用于后续业务提交")
    private Long fileId;

    @Schema(description = "访问URL，用于前端预览")
    private String url;

    @Schema(description = "原始文件名")
    private String name;

    @Schema(description = "文件大小（字节）")
    private Long size;
}