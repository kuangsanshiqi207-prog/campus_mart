package com.example.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评价信息")
public class ReviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "评价人ID")
    private Long fromUserId;

    @Schema(description = "评价人昵称")
    private String fromUserNickname;

    @Schema(description = "评价人头像")
    private String fromUserAvatar;

    @Schema(description = "被评价人ID")
    private Long toUserId;

    @Schema(description = "评分")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> images;

    @Schema(description = "回复内容")
    private String reply;

    @Schema(description = "回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "评价时间")
    private LocalDateTime createTime;
}