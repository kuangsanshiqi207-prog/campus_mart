package com.example.service.adminProduct.impl;

import com.example.api.ai.AiFeignClient;
import com.example.api.message.MessageFeignClient;
import com.example.api.user.UserFeignClient;
import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.constant.ProductConstant;
import com.example.dto.adminProduct.AdminProductQueryDTO;
import com.example.dto.adminProduct.OfflineDTO;
import com.example.dto.adminProduct.ProductAuditDTO;
import com.example.dto.ai.AiAuditDTO;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.entity.ProductImage;
import com.example.exception.BaseException;
import com.example.mapper.product.CategoryMapper;
import com.example.mapper.product.ProductImageMapper;
import com.example.mapper.product.ProductMapper;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.adminProduct.AdminProductService;
import com.example.vo.ai.AiAuditVO;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.ProductVO;
import com.example.vo.product.SellerVO;
import com.example.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final UserFeignClient userFeignClient;              // ← 改
    private final MessageFeignClient messageFeignClient;        // ← 改
    private final AiFeignClient aiFeignClient;

    // ==================== 商品列表 ====================

    @Override
    public PageResult listProducts(AdminProductQueryDTO query) {
        query.setPageNum(normalizePageNum(query.getPageNum()));
        query.setPageSize(normalizePageSize(query.getPageSize()));

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        Long total = productMapper.countAdmin(query);
        List<Product> list = productMapper.listAdmin(query, offset, query.getPageSize());

        List<ProductVO> voList = list.stream()
                .map(this::toProductVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    // ==================== 商品详情 ====================

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
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

    // ==================== 人工审核 ====================

    @Override
    @Transactional
    public void auditProduct(Long id, ProductAuditDTO dto) {
        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }

        if (!ProductConstant.AUDIT_PENDING.equals(product.getAuditStatus())) {
            throw new BaseException("该商品已审核过");
        }

        if (!"pass".equals(dto.getResult()) && !"reject".equals(dto.getResult())) {
            throw new BaseException("审核结果不合法");
        }

        if ("pass".equals(dto.getResult())) {
            productMapper.updateAudit(id, ProductConstant.AUDIT_APPROVED, null);

            messageFeignClient.send(product.getSellerId(),
                    NotificationConstant.TYPE_AUDIT,
                    "商品审核通过",
                    "您发布的商品【" + product.getTitle() + "】已审核通过。",
                    id);
        } else {
            productMapper.updateAudit(id, ProductConstant.AUDIT_REJECTED, dto.getReason());
            productMapper.updateStatus(id, ProductConstant.STATUS_OFFLINE);

            messageFeignClient.send(product.getSellerId(),
                    NotificationConstant.TYPE_AUDIT,
                    "商品审核未通过",
                    "您发布的商品【" + product.getTitle() + "】未通过审核，原因：" + dto.getReason(),
                    id);
        }
    }

    // ==================== AI 辅助审核 ====================

    @Override
    public AiAuditVO aiAudit(Long id) {
        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }

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

        List<String> imageUrls = productImageMapper.listByProductId(id).stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());
        dto.setImageUrls(imageUrls);

        return callAi(dto);
    }

    // ==================== 强制下架 ====================

    @Override
    @Transactional
    public void forceOffline(Long id, OfflineDTO dto) {
        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }

        productMapper.forceOffline(id);

        messageFeignClient.send(product.getSellerId(),
                NotificationConstant.TYPE_SYSTEM,
                "商品已被下架",
                "您发布的商品【" + product.getTitle() + "】已被管理员下架。原因：" + dto.getReason(),
                id);
    }

    // ==================== 删除商品 ====================

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }

        productMapper.deleteById(id);
        productImageMapper.deleteByProductId(id);

        messageFeignClient.send(product.getSellerId(),
                NotificationConstant.TYPE_SYSTEM,
                "商品已被删除",
                "您发布的商品【" + product.getTitle() + "】已被管理员删除。",
                id);
    }

    // ==================== 私有方法 ====================

    private AiAuditVO callAi(AiAuditDTO dto) {
        try {
            Result<AiAuditVO> result = aiFeignClient.audit(dto);
            if (result != null && result.getCode() != null && result.getCode() == 1 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("AI 审核调用失败：{}", e.getMessage());
        }
        return null;
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    private ProductVO toProductVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);

        if (product.getCategoryId() != null) {
            Category c = categoryMapper.getById(product.getCategoryId());
            if (c != null) vo.setCategoryName(c.getName());
        }

        // 通过 Feign 查卖家昵称
        Result<UserVO> sellerResult = userFeignClient.getById(product.getSellerId());
        if (sellerResult != null && sellerResult.getCode() == 1 && sellerResult.getData() != null) {
            vo.setSellerNickname(sellerResult.getData().getNickname());
        }

        List<ProductImage> images = productImageMapper.listByProductId(product.getId());
        if (!images.isEmpty()) vo.setCover(images.get(0).getUrl());

        return vo;
    }

    private SellerVO buildSellerVO(Long sellerId) {
        if (sellerId == null) return null;

        // 通过 Feign 查卖家
        Result<UserVO> sellerResult = userFeignClient.getById(sellerId);
        if (sellerResult == null || sellerResult.getCode() != 1 || sellerResult.getData() == null) {
            return null;
        }
        UserVO seller = sellerResult.getData();

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

    private Integer normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) return 1;
        return pageNum;
    }

    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1 || pageSize > 100) return 10;
        return pageSize;
    }
}