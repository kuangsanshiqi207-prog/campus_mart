package com.example.service.shop.impl;

import cn.hutool.core.util.IdUtil;
import com.example.api.file.FileFeignClient;
import com.example.api.user.UserFeignClient;
import com.example.constant.MessageConstant;
import com.example.constant.ProductConstant;
import com.example.constant.UserConstant;
import com.example.context.BaseContext;
import com.example.dto.ai.AiAuditDTO;
import com.example.dto.product.MyProductQueryDTO;
import com.example.dto.product.ProductCreateDTO;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.entity.ProductImage;
import com.example.event.ProductAuditEvent;
import com.example.exception.BaseException;
import com.example.mapper.product.CategoryMapper;
import com.example.mapper.product.ProductImageMapper;
import com.example.mapper.product.ProductMapper;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.shop.ShopService;
import com.example.vo.common.FileVO;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.ProductVO;
import com.example.vo.product.SellerVO;
import com.example.vo.product.UserProductStatsVO;
import com.example.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final UserFeignClient userFeignClient;              // ← 改
    private final FileFeignClient fileFeignClient;              // ← 改
    private final ApplicationEventPublisher applicationEventPublisher;

    // ==================== 发布商品 ====================

    @Override
    @Transactional
    public Long createProduct(ProductCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        Category category = categoryMapper.getById(dto.getCategoryId());
        if (category == null) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }
        if (!UserConstant.CERTIFIED.equals(category.getStatus())) {
            throw new BaseException(MessageConstant.CATEGORY_DISABLED);
        }

        // 校验图片归属（通过 Feign）
        List<String> imageUrls = new ArrayList<>();
        for (Long fileId : dto.getImageFileIds()) {
            Result<FileVO> fr = fileFeignClient.getFileOwnedByUser(fileId, userId);
            if (fr == null || fr.getCode() != 1 || fr.getData() == null) {
                throw new BaseException("无权使用该文件");
            }
            imageUrls.add(fr.getData().getUrl());
        }

        Long productId = IdUtil.getSnowflakeNextId();
        Product product = Product.builder()
                .id(productId)
                .sellerId(userId)
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .originalPrice(dto.getOriginalPrice())
                .quality(dto.getQuality())
                .tradeType(dto.getTradeType())
                .tradePlace(dto.getTradePlace())
                .status(ProductConstant.STATUS_OFFLINE)
                .auditStatus(ProductConstant.AUDIT_PENDING)
                .viewCount(0)
                .favoriteCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        productMapper.insert(product);

        saveProductImages(productId, imageUrls, dto.getImageFileIds());

        // 事务提交后异步触发 AI 审核
        publishAuditEvent(product, imageUrls);

        return productId;
    }

    // ==================== 修改商品 ====================

    @Override
    @Transactional
    public void updateProduct(Long id, ProductCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        Product existing = productMapper.getById(id);
        if (existing == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        if (!existing.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_YOURS);
        }
        if (ProductConstant.STATUS_SOLD.equals(existing.getStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_SOLD_CANNOT_EDIT);
        }

        Category category = categoryMapper.getById(dto.getCategoryId());
        if (category == null) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        List<String> imageUrls = new ArrayList<>();
        for (Long fileId : dto.getImageFileIds()) {
            Result<FileVO> fr = fileFeignClient.getFileOwnedByUser(fileId, userId);
            if (fr == null || fr.getCode() != 1 || fr.getData() == null) {
                throw new BaseException("无权使用该文件");
            }
            imageUrls.add(fr.getData().getUrl());
        }

        Product update = Product.builder()
                .id(id)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .originalPrice(dto.getOriginalPrice())
                .quality(dto.getQuality())
                .categoryId(dto.getCategoryId())
                .tradeType(dto.getTradeType())
                .tradePlace(dto.getTradePlace())
                .build();
        productMapper.update(update);

        // 重新进入审核（下架 + 审核中）
        productMapper.updateAuditStatus(id, ProductConstant.AUDIT_PENDING, null);
        productMapper.updateStatus(id, ProductConstant.STATUS_OFFLINE);

        productImageMapper.deleteByProductId(id);
        saveProductImages(id, imageUrls, dto.getImageFileIds());

        // 事务提交后异步触发 AI 审核
        publishAuditEvent(update, imageUrls);
    }

    // ==================== 删除商品 ====================

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Long userId = BaseContext.getCurrentId();

        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_YOURS);
        }
        if (ProductConstant.STATUS_RESERVED.equals(product.getStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_RESERVED_CANNOT_DELETE);
        }
        if (ProductConstant.STATUS_SOLD.equals(product.getStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_SOLD_CANNOT_DELETE);
        }

        productMapper.deleteById(id);
        productImageMapper.deleteByProductId(id);
    }

    // ==================== 下架 ====================

    @Override
    @Transactional
    public void offlineProduct(Long id) {
        Long userId = BaseContext.getCurrentId();

        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_YOURS);
        }
        if (ProductConstant.STATUS_OFFLINE.equals(product.getStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_ALREADY_OFFLINE);
        }

        productMapper.updateStatus(id, ProductConstant.STATUS_OFFLINE);
    }

    // ==================== 上架 ====================

    @Override
    @Transactional
    public void onlineProduct(Long id) {
        Long userId = BaseContext.getCurrentId();

        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_YOURS);
        }
        if (ProductConstant.STATUS_ON_SALE.equals(product.getStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_ALREADY_ON_SALE);
        }
        if (!ProductConstant.AUDIT_APPROVED.equals(product.getAuditStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_AUDIT_NOT_PASSED);
        }

        productMapper.updateStatus(id, ProductConstant.STATUS_ON_SALE);
    }

    // ==================== 我的商品列表 ====================

    @Override
    public PageResult listMyProducts(MyProductQueryDTO query) {
        Long userId = BaseContext.getCurrentId();

        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1 || query.getPageSize() > 100) {
            query.setPageSize(10);
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        int limit = query.getPageSize();

        Long total = productMapper.countMine(userId, query.getStatus());
        List<ProductVO> list = productMapper.listMine(userId, query.getStatus(), offset, limit);

        return new PageResult(total, list);
    }

    // ==================== 我的商品详情 ====================

    @Override
    public ProductDetailVO getMyProductDetail(Long id) {
        Long userId = BaseContext.getCurrentId();

        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_YOURS);
        }

        List<ProductImage> images = productImageMapper.listByProductId(id);
        List<String> imageUrls = images.stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());

        String categoryName = null;
        if (product.getCategoryId() != null) {
            Category c = categoryMapper.getById(product.getCategoryId());
            if (c != null) categoryName = c.getName();
        }

        SellerVO seller = buildSellerVO(product.getSellerId());

        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);
        vo.setCategoryName(categoryName);
        vo.setImages(imageUrls);
        vo.setSeller(seller);
        vo.setFavorited(false);

        return vo;
    }

    // ==================== 我的商品统计 ====================

    @Override
    public UserProductStatsVO getMyStats() {
        Long userId = BaseContext.getCurrentId();
        UserProductStatsVO stats = productMapper.getMyStats(userId);
        if (stats == null) {
            stats = UserProductStatsVO.builder()
                    .total(0).onSale(0).reserved(0).sold(0)
                    .offline(0).pending(0).rejected(0)
                    .viewTotal(0L).favoriteTotal(0L)
                    .build();
        }
        return stats;
    }

    // ==================== 私有方法 ====================

    private void publishAuditEvent(Product product, List<String> imageUrls) {
        applicationEventPublisher.publishEvent(
                new ProductAuditEvent(product.getId(), buildAiAuditDTO(product, imageUrls)));
    }

    private AiAuditDTO buildAiAuditDTO(Product product, List<String> imageUrls) {
        AiAuditDTO dto = new AiAuditDTO();
        dto.setAuditType("product");

        Map<String, String> textFields = new HashMap<>();
        putIfPresent(textFields, "title", product.getTitle());
        putIfPresent(textFields, "description", product.getDescription());
        putIfPresent(textFields, "price", product.getPrice() == null ? null : product.getPrice().toPlainString());
        putIfPresent(textFields, "quality", product.getQuality());
        putIfPresent(textFields, "tradeType", product.getTradeType());
        putIfPresent(textFields, "tradePlace", product.getTradePlace());
        dto.setTextFields(textFields);

        dto.setImageUrls(imageUrls == null ? List.of() : imageUrls);
        return dto;
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    private void saveProductImages(Long productId, List<String> urls, List<Long> fileIds) {
        if (urls.isEmpty()) return;

        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            images.add(ProductImage.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .productId(productId)
                    .url(urls.get(i))
                    .sort(i)
                    .createTime(LocalDateTime.now())
                    .build());
            // 通过 Feign 标记文件已使用
            fileFeignClient.markUsed(fileIds.get(i));
        }
        productImageMapper.batchInsert(images);
    }

    private SellerVO buildSellerVO(Long sellerId) {
        if (sellerId == null) return null;

        Result<UserVO> sr = userFeignClient.getById(sellerId);
        if (sr == null || sr.getCode() != 1 || sr.getData() == null) return null;
        UserVO seller = sr.getData();

        Integer productCount = productMapper.countOnSaleBySellerId(sellerId);
        return SellerVO.builder()
                .id(seller.getId())
                .nickname(seller.getNickname())
                .avatar(seller.getAvatar())
                .creditScore(seller.getCreditScore())
                .certified(seller.getCertified())
                .productCount(productCount == null ? 0 : productCount)
                .build();
    }
}