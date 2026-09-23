package com.example.service.market.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.MessageConstant;
import com.example.constant.ProductConstant;
import com.example.context.BaseContext;
import com.example.dto.product.ProductQueryDTO;
import com.example.entity.BrowseHistory;
import com.example.entity.Favorite;
import com.example.entity.Product;
import com.example.entity.ProductImage;
import com.example.entity.User;
import com.example.exception.BaseException;
import com.example.mapper.product.BrowseHistoryMapper;
import com.example.mapper.product.CategoryMapper;
import com.example.mapper.product.FavoriteMapper;
import com.example.mapper.product.ProductImageMapper;
import com.example.mapper.product.ProductMapper;
import com.example.mapper.user.UserMapper;
import com.example.result.PageResult;
import com.example.service.market.MarketService;
import com.example.vo.product.CategoryVO;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.ProductVO;
import com.example.vo.product.SellerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final FavoriteMapper favoriteMapper;
    private final BrowseHistoryMapper browseHistoryMapper;
    private final UserMapper userMapper;

    // ==================== 商品列表/搜索 ====================

    @Override
    public PageResult listProducts(ProductQueryDTO query) {
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1 || query.getPageSize() > 100) {
            query.setPageSize(10);
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        int limit = query.getPageSize();

        Long total = productMapper.countProducts(query);
        List<ProductVO> list = productMapper.listProducts(query, offset, limit);

        return new PageResult(total, list);
    }

    // ==================== 商品详情 ====================

    @Override
    @Transactional
    public ProductDetailVO getProductDetail(Long id) {
        Product product = productMapper.getById(id);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        if (!ProductConstant.AUDIT_APPROVED.equals(product.getAuditStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }

        // 1. 图片
        List<ProductImage> images = productImageMapper.listByProductId(id);
        List<String> imageUrls = images.stream()
                .map(ProductImage::getUrl)
                .toList();

        // 2. 分类名
        String categoryName = null;
        if (product.getCategoryId() != null) {
            var category = categoryMapper.getById(product.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        // 3. 卖家信息
        SellerVO seller = buildSellerVO(product.getSellerId());

        // 4. 浏览量 +1
        productMapper.incrementViewCount(id);

        // 5. 是否已收藏（仅登录用户）
        Long userId = BaseContext.getCurrentId();
        Boolean favorited = false;
        if (userId != null) {
            Favorite fav = favoriteMapper.getByUserAndProduct(userId, id);
            favorited = fav != null;
        }

        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);
        vo.setCategoryName(categoryName);
        vo.setImages(imageUrls);
        vo.setSeller(seller);
        vo.setFavorited(favorited);
        vo.setViewCount(product.getViewCount() + 1);

        // 6. 记录浏览历史（仅登录用户）
        if (userId != null) {
            recordBrowse(userId, id);
        }

        return vo;
    }

    // ==================== 首页推荐 ====================

    @Override
    public PageResult listRecommend(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 100) pageSize = 10;

        int offset = (pageNum - 1) * pageSize;
        List<ProductVO> list = productMapper.listRecommend(offset, pageSize);

        return new PageResult(list.size(), list);
    }

    // ==================== 相似商品 ====================

    @Override
    public List<ProductVO> listSimilar(Long id, Integer limit) {
        if (limit == null || limit < 1 || limit > 20) limit = 10;

        Product product = productMapper.getById(id);
        if (product == null) {
            return List.of();
        }

        return productMapper.listSimilar(product.getCategoryId(), id, limit);
    }

    // ==================== 卖家主页 ====================

    @Override
    public SellerVO getSeller(Long productId) {
        Product product = productMapper.getById(productId);
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        return buildSellerVO(product.getSellerId());
    }

    // ==================== 分类列表 ====================

    @Override
    public List<CategoryVO> listCategories() {
        return categoryMapper.listEnabled().stream()
                .map(c -> CategoryVO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .parentId(c.getParentId())
                        .sort(c.getSort())
                        .build())
                .toList();
    }

    // ==================== 收藏 ====================

    @Override
    @Transactional
    public void addFavorite(Long productId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }

        Product product = productMapper.getById(productId);
        if (product == null || !ProductConstant.AUDIT_APPROVED.equals(product.getAuditStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }

        Favorite existing = favoriteMapper.getByUserAndProduct(userId, productId);
        if (existing != null) {
            throw new BaseException(MessageConstant.PRODUCT_ALREADY_FAVORITED);
        }

        Favorite favorite = Favorite.builder()
                .id(IdUtil.getSnowflakeNextId())
                .userId(userId)
                .productId(productId)
                .createTime(LocalDateTime.now())
                .build();
        favoriteMapper.insert(favorite);

        productMapper.incrementFavoriteCount(productId);
    }

    @Override
    @Transactional
    public void removeFavorite(Long productId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }

        Favorite existing = favoriteMapper.getByUserAndProduct(userId, productId);
        if (existing == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FAVORITED);
        }

        favoriteMapper.deleteByUserAndProduct(userId, productId);
        productMapper.decrementFavoriteCount(productId);
    }

    @Override
    public PageResult listFavorites(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 100) pageSize = 10;

        int offset = (pageNum - 1) * pageSize;
        Long total = favoriteMapper.countByUserId(userId);
        List<ProductVO> list = favoriteMapper.listByUserId(userId, offset, pageSize);

        return new PageResult(total, list);
    }

    // ==================== 浏览历史 ====================

    @Override
    public PageResult listHistory(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 100) pageSize = 10;

        int offset = (pageNum - 1) * pageSize;
        Long total = browseHistoryMapper.countByUserId(userId);
        List<ProductVO> list = browseHistoryMapper.listByUserId(userId, offset, pageSize);

        return new PageResult(total, list);
    }

    @Override
    @Transactional
    public void clearHistory() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        browseHistoryMapper.deleteAllByUserId(userId);
    }

    // ==================== 私有方法 ====================

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

    /**
     * 记录浏览历史：有记录则更新时间，无则插入
     */
    private void recordBrowse(Long userId, Long productId) {
        BrowseHistory existing = browseHistoryMapper.getByUserAndProduct(userId, productId);
        if (existing != null) {
            browseHistoryMapper.updateBrowseTime(userId, productId);
        } else {
            BrowseHistory history = BrowseHistory.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .userId(userId)
                    .productId(productId)
                    .browseTime(LocalDateTime.now())
                    .build();
            browseHistoryMapper.insert(history);
        }
    }
}