package com.example.service.adminProduct.impl;

import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.constant.ProductConstant;
import com.example.dto.adminProduct.AdminProductQueryDTO;
import com.example.dto.adminProduct.OfflineDTO;
import com.example.dto.adminProduct.ProductAuditDTO;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.entity.ProductImage;
import com.example.entity.User;
import com.example.exception.BaseException;
import com.example.mapper.product.CategoryMapper;
import com.example.mapper.product.ProductImageMapper;
import com.example.mapper.product.ProductMapper;
import com.example.mapper.user.UserMapper;
import com.example.result.PageResult;
import com.example.service.adminProduct.AdminProductService;
import com.example.service.message.MessageSender;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.ProductVO;
import com.example.vo.product.SellerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final MessageSender messageSender;

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
            // 审核通过
            productMapper.updateAudit(id, ProductConstant.AUDIT_APPROVED, null);

            messageSender.send(product.getSellerId(),
                    NotificationConstant.TYPE_AUDIT,
                    "商品审核通过",
                    "您发布的商品【" + product.getTitle() + "】已审核通过。",
                    id);
        } else {
            // 审核拒绝，商品下架
            productMapper.updateAudit(id, ProductConstant.AUDIT_REJECTED, dto.getReason());
            productMapper.updateStatus(id, ProductConstant.STATUS_OFFLINE);

            messageSender.send(product.getSellerId(),
                    NotificationConstant.TYPE_AUDIT,
                    "商品审核未通过",
                    "您发布的商品【" + product.getTitle() + "】未通过审核，原因：" + dto.getReason(),
                    id);
        }
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

        messageSender.send(product.getSellerId(),
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

        messageSender.send(product.getSellerId(),
                NotificationConstant.TYPE_SYSTEM,
                "商品已被删除",
                "您发布的商品【" + product.getTitle() + "】已被管理员删除。",
                id);
    }

    // ==================== 私有方法 ====================

    private ProductVO toProductVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);

        // 关联查分类名、卖家昵称、封面图
        if (product.getCategoryId() != null) {
            Category c = categoryMapper.getById(product.getCategoryId());
            if (c != null) vo.setCategoryName(c.getName());
        }
        User seller = userMapper.getById(product.getSellerId());
        if (seller != null) vo.setSellerNickname(seller.getNickname());

        List<ProductImage> images = productImageMapper.listByProductId(product.getId());
        if (!images.isEmpty()) vo.setCover(images.get(0).getUrl());

        return vo;
    }

    private SellerVO buildSellerVO(Long sellerId) {
        if (sellerId == null) return null;
        User seller = userMapper.getById(sellerId);
        if (seller == null) return null;
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