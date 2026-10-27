package com.example.controller.internal;

import com.example.api.product.ProductFeignClient;
import com.example.constant.ProductConstant;
import com.example.entity.Product;
import com.example.entity.ProductImage;
import com.example.entity.Review;
import com.example.mapper.product.ProductImageMapper;
import com.example.mapper.product.ProductMapper;
import com.example.mapper.review.ReviewMapper;
import com.example.result.Result;
import com.example.vo.product.UserProductStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/product")
@RequiredArgsConstructor
public class ProductInternalController implements ProductFeignClient {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ReviewMapper reviewMapper;

    @Override
    public Result<Product> getById(Long id) {
        Product product = productMapper.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    @Override
    public Result<Void> markReserved(Long id) {
        productMapper.updateStatus(id, ProductConstant.STATUS_RESERVED);
        return Result.success();
    }

    @Override
    public Result<Void> markOnSale(Long id) {
        productMapper.updateStatus(id, ProductConstant.STATUS_ON_SALE);
        return Result.success();
    }

    @Override
    public Result<Void> markSold(Long id) {
        productMapper.updateStatus(id, ProductConstant.STATUS_SOLD);
        return Result.success();
    }

    @Override
    public Result<Void> markOffline(Long id) {
        productMapper.updateStatus(id, ProductConstant.STATUS_OFFLINE);
        return Result.success();
    }

    @Override
    public Result<UserProductStatsVO> getMyStats(Long sellerId) {
        return Result.success(productMapper.getMyStats(sellerId));
    }

    @Override
    public Result<Void> updateAuditResult(Long id, String auditStatus, String auditReason, String status) {
        productMapper.updateAuditResult(id, auditStatus, auditReason, status);
        return Result.success();
    }

    @Override
    public Result<List<String>> listImageUrls(Long productId) {
        List<String> urls = productImageMapper.listByProductId(productId).stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());
        return Result.success(urls);
    }

    @Override
    public Result<Review> getReviewById(Long id) {
        Review review = reviewMapper.getById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        return Result.success(review);
    }

    @Override
    public Result<Void> deleteReview(Long id) {
        reviewMapper.deleteById(id);
        return Result.success();
    }
}
