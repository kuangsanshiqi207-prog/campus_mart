package com.example.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告信息")
public class AnnouncementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;
}