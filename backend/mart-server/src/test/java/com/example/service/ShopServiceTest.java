package com.example.service;

import com.example.context.BaseContext;
import com.example.dto.product.MyProductQueryDTO;
import com.example.dto.product.ProductCreateDTO;
import com.example.exception.BaseException;
import com.example.result.PageResult;
import com.example.service.shop.ShopService;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.UserProductStatsVO;
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
class ShopServiceTest {

    @Autowired
    private ShopService shopService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 1001L;

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    private ProductCreateDTO buildDTO() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setTitle("测试商品");
        dto.setDescription("这是一个测试商品");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setOriginalPrice(new BigDecimal("199.99"));
        dto.setQuality("good");
        dto.setCategoryId(1L);
        dto.setTradeType("face");
        dto.setTradePlace("图书馆门口");
        dto.setImageFileIds(List.of());
        return dto;
    }

    @Test
    void testGetMyStats() {
        BaseContext.setCurrentId(TEST_USER_ID);

        UserProductStatsVO stats = shopService.getMyStats();

        assertNotNull(stats, "统计数据不应为 null");
        assertTrue(stats.getTotal() >= 0, "商品总数应非负");
        // 如果你的 VO 还有 onSale / offline 等字段，可继续断言：
        // assertTrue(stats.getOnSale() >= 0, "在售数应非负");
        // assertTrue(stats.getOffline() >= 0, "下架数应非负");
    }

    @Test
    void testListMyProducts() {
        BaseContext.setCurrentId(TEST_USER_ID);

        MyProductQueryDTO query = new MyProductQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);

        PageResult result = shopService.listMyProducts(query);

        assertNotNull(result, "分页结果不应为 null");
        assertNotNull(result.getRecords(), "记录列表不应为 null");
        assertTrue(result.getTotal() >= 0, "总数应非负");
        assertTrue(result.getRecords().size() <= 10, "单页数量不应超过 pageSize");
    }

    @Test
    void testGetMyProductDetail() {
        BaseContext.setCurrentId(TEST_USER_ID);

        ProductDetailVO vo = shopService.getMyProductDetail(TEST_PRODUCT_ID);

        assertNotNull(vo, "商品详情不应为 null");
        assertEquals(TEST_PRODUCT_ID, vo.getId(), "商品 ID 应匹配");
        assertNotNull(vo.getTitle(), "标题不应为 null");
    }

    @Test
    void testGetMyProductDetailNotYours() {
        BaseContext.setCurrentId(99999L);

        BaseException ex = assertThrows(BaseException.class,
                () -> shopService.getMyProductDetail(TEST_PRODUCT_ID),
                "查看他人商品应抛出 BaseException");

        assertNotNull(ex.getMessage(), "异常信息不应为空");
    }

    @Test
    void testOfflineNotYours() {
        BaseContext.setCurrentId(99999L);

        BaseException ex = assertThrows(BaseException.class,
                () -> shopService.offlineProduct(TEST_PRODUCT_ID),
                "下架他人商品应抛出 BaseException");

        assertNotNull(ex.getMessage(), "异常信息不应为空");
    }
}