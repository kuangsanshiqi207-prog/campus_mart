package com.example.service;

import com.example.context.BaseContext;
import com.example.dto.product.ProductQueryDTO;
import com.example.exception.BaseException;
import com.example.result.PageResult;
import com.example.service.market.MarketService;
import com.example.vo.product.CategoryVO;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.ProductVO;
import com.example.vo.product.SellerVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MarketServiceTest {

    @Autowired
    private MarketService marketService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 1001L;

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    // ==================== 商品列表/搜索 ====================

    @Test
    void testListProducts() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);

        PageResult result = marketService.listProducts(query);

        assertNotNull(result, "分页结果不应为 null");
        assertNotNull(result.getRecords(), "记录列表不应为 null");
        assertTrue(result.getTotal() >= 0, "总数应非负");
        assertTrue(result.getRecords().size() <= 10, "单页数量不应超过 pageSize");
    }

    @Test
    void testListProductsByKeyword() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setKeyword("自行车");
        query.setPageNum(1);
        query.setPageSize(10);

        PageResult result = marketService.listProducts(query);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 0, "搜索结果数应非负");
    }

    @Test
    void testListProductsByCategory() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setCategoryId(2L);
        query.setPageNum(1);
        query.setPageSize(10);

        PageResult result = marketService.listProducts(query);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 0);
    }

    @Test
    void testListProductsByPriceRange() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setMinPrice(new BigDecimal("10"));
        query.setMaxPrice(new BigDecimal("100"));
        query.setPageNum(1);
        query.setPageSize(10);

        PageResult result = marketService.listProducts(query);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 0, "价格区间查询结果应非负");
    }

    @Test
    void testListProductsSortByPriceAsc() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setSort("price_asc");
        query.setPageNum(1);
        query.setPageSize(10);

        PageResult result = marketService.listProducts(query);

        assertNotNull(result);
        List<ProductVO> records = result.getRecords();
        assertNotNull(records);
        // 验证价格升序
        for (int i = 1; i < records.size(); i++) {
            BigDecimal prev = records.get(i - 1).getPrice();
            BigDecimal curr = records.get(i).getPrice();
            if (prev != null && curr != null) {
                assertTrue(prev.compareTo(curr) <= 0, "价格应按升序排列");
            }
        }
    }

    // ==================== 商品详情 ====================

    @Test
    void testGetProductDetail() {
        ProductDetailVO vo = marketService.getProductDetail(TEST_PRODUCT_ID);

        assertNotNull(vo, "商品详情不应为 null");
        assertEquals(TEST_PRODUCT_ID, vo.getId(), "商品 ID 应匹配");
        assertNotNull(vo.getTitle(), "标题不应为 null");
        assertNotNull(vo.getImages(), "图片列表不应为 null");
    }

    @Test
    void testGetProductDetailNotFound() {
        assertThrows(BaseException.class,
                () -> marketService.getProductDetail(999999L),
                "查询不存在的商品应抛出 BaseException");
    }

    // ==================== 推荐 ====================

    @Test
    void testListRecommend() {
        PageResult result = marketService.listRecommend(1, 5);

        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().size() <= 5, "推荐商品数不应超过 5");
    }

    // ==================== 相似商品 ====================

    @Test
    void testListSimilar() {
        List<ProductVO> list = marketService.listSimilar(TEST_PRODUCT_ID, 5);

        assertNotNull(list, "相似商品列表不应为 null");
        assertTrue(list.size() <= 5, "相似商品数不应超过 5");
        assertTrue(list.stream().noneMatch(p -> TEST_PRODUCT_ID.equals(p.getId())),
                "相似商品不应包含自身");
    }

    // ==================== 卖家主页 ====================

    @Test
    void testGetSeller() {
        SellerVO seller = marketService.getSeller(TEST_PRODUCT_ID);

        assertNotNull(seller, "卖家信息不应为 null");
        assertNotNull(seller.getNickname(), "卖家昵称不应为 null");
        assertNotNull(seller.getCreditScore(), "信用分不应为 null");
        assertTrue(seller.getProductCount() >= 0, "在售商品数应非负");
    }

    // ==================== 分类 ====================

    @Test
    void testListCategories() {
        List<CategoryVO> list = marketService.listCategories();

        assertNotNull(list, "分类列表不应为 null");
        assertFalse(list.isEmpty(), "分类列表不应为空");
        assertNotNull(list.get(0).getName(), "分类名不应为 null");
    }

    // ==================== 收藏 ====================

    @Test
    void testAddFavorite() {
        BaseContext.setCurrentId(TEST_USER_ID);

        marketService.addFavorite(TEST_PRODUCT_ID);

        PageResult result = marketService.listFavorites(1, 10);
        assertNotNull(result);
        assertTrue(result.getTotal() >= 1, "收藏后总数应至少为 1");
    }

    @Test
    void testAddFavoriteDuplicate() {
        BaseContext.setCurrentId(TEST_USER_ID);

        marketService.addFavorite(TEST_PRODUCT_ID);

        assertThrows(BaseException.class,
                () -> marketService.addFavorite(TEST_PRODUCT_ID),
                "重复收藏应抛出 BaseException");
    }

    @Test
    void testRemoveFavorite() {
        BaseContext.setCurrentId(TEST_USER_ID);

        marketService.addFavorite(TEST_PRODUCT_ID);
        long before = marketService.listFavorites(1, 100).getTotal();

        marketService.removeFavorite(TEST_PRODUCT_ID);

        long after = marketService.listFavorites(1, 100).getTotal();
        assertEquals(before - 1, after, "取消收藏后总数应减 1");
    }

    @Test
    void testListFavorites() {
        BaseContext.setCurrentId(TEST_USER_ID);

        marketService.addFavorite(TEST_PRODUCT_ID);

        PageResult result = marketService.listFavorites(1, 10);
        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() >= 1);
    }

    // ==================== 浏览历史 ====================

    @Test
    void testBrowseHistory() {
        BaseContext.setCurrentId(TEST_USER_ID);

        marketService.getProductDetail(TEST_PRODUCT_ID);
        marketService.getProductDetail(1002L);
        marketService.getProductDetail(TEST_PRODUCT_ID); // 重复访问，应去重

        PageResult result = marketService.listHistory(1, 10);
        assertNotNull(result);
        assertEquals(2, result.getTotal(), "重复访问应去重，历史记录应为 2 条");
    }

    @Test
    void testClearHistory() {
        BaseContext.setCurrentId(TEST_USER_ID);

        marketService.getProductDetail(TEST_PRODUCT_ID);

        marketService.clearHistory();

        PageResult result = marketService.listHistory(1, 10);
        assertNotNull(result);
        assertEquals(0, result.getTotal(), "清空后历史记录应为 0");
    }
}