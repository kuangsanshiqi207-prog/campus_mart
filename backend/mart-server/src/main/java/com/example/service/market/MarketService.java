package com.example.service.market;

import com.example.dto.product.ProductQueryDTO;
import com.example.result.PageResult;
import com.example.vo.product.CategoryVO;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.ProductVO;
import com.example.vo.product.SellerVO;

import java.util.List;

public interface MarketService {

    /** 商品列表/搜索 */
    PageResult listProducts(ProductQueryDTO query);

    /** 商品详情 */
    ProductDetailVO getProductDetail(Long id);

    /** 首页推荐 */
    PageResult listRecommend(Integer pageNum, Integer pageSize);

    /** 相似商品 */
    List<ProductVO> listSimilar(Long id, Integer limit);

    /** 卖家主页 */
    SellerVO getSeller(Long productId);

    /** 分类列表 */
    List<CategoryVO> listCategories();

    /** 收藏商品 */
    void addFavorite(Long productId);

    /** 取消收藏 */
    void removeFavorite(Long productId);

    /** 我的收藏 */
    PageResult listFavorites(Integer pageNum, Integer pageSize);

    /** 浏览历史 */
    PageResult listHistory(Integer pageNum, Integer pageSize);

    /** 清空浏览历史 */
    void clearHistory();
}