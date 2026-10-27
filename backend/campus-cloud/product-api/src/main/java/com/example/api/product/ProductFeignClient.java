package com.example.api.product;

import com.example.entity.Product;
import com.example.entity.Review;
import com.example.result.Result;
import com.example.vo.product.UserProductStatsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商品资源中心对外只读/状态流转契约（唯一写方 = product-service）。
 */
@FeignClient(name = "product-service", path = "/internal/product")
public interface ProductFeignClient {

    /** 按 ID 查商品 */
    @GetMapping("/getById/{id}")
    Result<Product> getById(@PathVariable("id") Long id);

    /** 标记为「已预订」（下单成交） */
    @PutMapping("/markReserved/{id}")
    Result<Void> markReserved(@PathVariable("id") Long id);

    /** 标记为「重新上架」（取消订单/释放） */
    @PutMapping("/markOnSale/{id}")
    Result<Void> markOnSale(@PathVariable("id") Long id);

    /** 标记为「已售出」（成交完成） */
    @PutMapping("/markSold/{id}")
    Result<Void> markSold(@PathVariable("id") Long id);

    /** 强制下架（举报处罚等） */
    @PutMapping("/markOffline/{id}")
    Result<Void> markOffline(@PathVariable("id") Long id);

    /** 卖家商品统计（供 biz-service 的 stats 只读） */
    @GetMapping("/getMyStats/{sellerId}")
    Result<UserProductStatsVO> getMyStats(@PathVariable("sellerId") Long sellerId);

    /** 应用审核结果（供 audit-service 回调） */
    @PutMapping("/updateAuditResult/{id}")
    Result<Void> updateAuditResult(@PathVariable("id") Long id,
                                   @RequestParam("auditStatus") String auditStatus,
                                   @RequestParam("auditReason") String auditReason,
                                   @RequestParam("status") String status);

    /** 商品图片 URL 列表（供 audit-service 组装 AI 入参） */
    @GetMapping("/listImageUrls/{productId}")
    Result<List<String>> listImageUrls(@PathVariable("productId") Long productId);

    /** 按 ID 查评价（供 audit-service） */
    @GetMapping("/getReviewById/{id}")
    Result<Review> getReviewById(@PathVariable("id") Long id);

    /** 删除评价（供 audit-service 回调） */
    @DeleteMapping("/deleteReview/{id}")
    Result<Void> deleteReview(@PathVariable("id") Long id);
}
