package com.example.service;

import com.example.constant.OrderConstant;
import com.example.constant.ProductConstant;
import com.example.context.BaseContext;
import com.example.dto.order.OrderCreateDTO;
import com.example.dto.order.OrderQueryDTO;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.exception.BaseException;
import com.example.mapper.order.OrderMapper;
import com.example.mapper.product.ProductMapper;
import com.example.result.PageResult;
import com.example.service.order.OrderService;
import com.example.vo.order.OrderVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("交易模块 - Service 层测试")
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    private static final Long SELLER_ID = 1L;
    private static final Long BUYER_ID = 2L;
    private static final Long PRODUCT_ID = 1001L;

    @AfterEach
    public void tearDown() {
        BaseContext.removeCurrentId();
    }

    // ==================== 申请交易 ====================

    @Nested
    @DisplayName("申请交易")
    class CreateOrderTests {

        @Test
        @DisplayName("成功申请交易")
        void testCreateOrderSuccess() {
            BaseContext.setCurrentId(BUYER_ID);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setProductId(PRODUCT_ID);
            dto.setRemark("明天下午方便吗");
            dto.setTradePlace("图书馆门口");

            Long orderId = orderService.createOrder(dto);

            assertNotNull(orderId, "订单ID不应为空");

            Order order = orderMapper.getById(orderId);
            assertNotNull(order);
            assertEquals(BUYER_ID, order.getBuyerId());
            assertEquals(SELLER_ID, order.getSellerId());
            assertEquals(PRODUCT_ID, order.getProductId());
            assertEquals(OrderConstant.STATUS_PENDING, order.getStatus());
            assertNotNull(order.getOrderNo());
        }

        @Test
        @DisplayName("失败 - 不能购买自己的商品")
        void testCreateOrderCannotBuyOwn() {
            BaseContext.setCurrentId(SELLER_ID);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setProductId(PRODUCT_ID);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.createOrder(dto));
            assertEquals("不能购买自己的商品", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 商品不存在")
        void testCreateOrderProductNotFound() {
            BaseContext.setCurrentId(BUYER_ID);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setProductId(999999L);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.createOrder(dto));
            assertEquals("商品不存在", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 商品已有待处理订单")
        void testCreateOrderAlreadyExists() {
            BaseContext.setCurrentId(BUYER_ID);

            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setProductId(PRODUCT_ID);
            orderService.createOrder(dto);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.createOrder(dto));
            assertEquals("该商品已有待处理的订单", ex.getMessage());
        }
    }

    // ==================== 卖家接受 ====================

    @Nested
    @DisplayName("卖家接受交易")
    class AcceptOrderTests {

        @Test
        @DisplayName("成功接受")
        void testAcceptOrderSuccess() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            orderService.acceptOrder(orderId);

            Order order = orderMapper.getById(orderId);
            assertEquals(OrderConstant.STATUS_ACCEPTED, order.getStatus());

            Product product = productMapper.getById(PRODUCT_ID);
            assertEquals(ProductConstant.STATUS_RESERVED, product.getStatus());
        }

        @Test
        @DisplayName("失败 - 非卖家不能接受")
        void testAcceptOrderNotSeller() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(BUYER_ID);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.acceptOrder(orderId));
            assertEquals("只有卖家可以操作", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 状态不是 pending")
        void testAcceptOrderWrongStatus() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            orderService.acceptOrder(orderId);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.acceptOrder(orderId));
            assertEquals("订单状态错误", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 订单不存在")
        void testAcceptOrderNotFound() {
            BaseContext.setCurrentId(SELLER_ID);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.acceptOrder(999999L));
            assertEquals("订单不存在", ex.getMessage());
        }
    }

    // ==================== 卖家拒绝 ====================

    @Nested
    @DisplayName("卖家拒绝交易")
    class RejectOrderTests {

        @Test
        @DisplayName("成功拒绝")
        void testRejectOrderSuccess() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            orderService.rejectOrder(orderId, "已经不卖了");

            Order order = orderMapper.getById(orderId);
            assertEquals(OrderConstant.STATUS_REJECTED, order.getStatus());
            assertEquals("已经不卖了", order.getRejectReason());
        }

        @Test
        @DisplayName("失败 - 非卖家不能拒绝")
        void testRejectOrderNotSeller() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(BUYER_ID);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.rejectOrder(orderId, "随便"));
            assertEquals("只有卖家可以操作", ex.getMessage());
        }
    }

    // ==================== 取消订单 ====================

    @Nested
    @DisplayName("取消订单")
    class CancelOrderTests {

        @Test
        @DisplayName("买家取消 pending 订单")
        void testCancelByBuyer() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(BUYER_ID);
            orderService.cancelOrder(orderId, "不想买了");

            Order order = orderMapper.getById(orderId);
            assertEquals(OrderConstant.STATUS_CANCELLED, order.getStatus());
            assertEquals("不想买了", order.getCancelReason());
        }

        @Test
        @DisplayName("卖家取消 accepted 订单")
        void testCancelBySeller() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            orderService.acceptOrder(orderId);

            orderService.cancelOrder(orderId, "商品有问题");

            Order order = orderMapper.getById(orderId);
            assertEquals(OrderConstant.STATUS_CANCELLED, order.getStatus());

            Product product = productMapper.getById(PRODUCT_ID);
            assertEquals(ProductConstant.STATUS_ON_SALE, product.getStatus());
        }

        @Test
        @DisplayName("失败 - 非参与方不能取消")
        void testCancelNotParticipant() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(99999L);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.cancelOrder(orderId, "无关"));
            assertEquals("无权操作该订单", ex.getMessage());
        }
    }

    // ==================== 买家完成 ====================

    @Nested
    @DisplayName("买家确认完成")
    class CompleteOrderTests {

        @Test
        @DisplayName("成功完成")
        void testCompleteOrderSuccess() {
            Long orderId = createAcceptedOrder();

            BaseContext.setCurrentId(BUYER_ID);
            orderService.completeOrder(orderId);

            Order order = orderMapper.getById(orderId);
            assertEquals(OrderConstant.STATUS_COMPLETED, order.getStatus());
            assertNotNull(order.getCompletedTime());

            Product product = productMapper.getById(PRODUCT_ID);
            assertEquals(ProductConstant.STATUS_SOLD, product.getStatus());
        }

        @Test
        @DisplayName("失败 - 非买家不能完成")
        void testCompleteOrderNotBuyer() {
            Long orderId = createAcceptedOrder();

            BaseContext.setCurrentId(SELLER_ID);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.completeOrder(orderId));
            assertEquals("只有买家可以确认完成", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 状态不是 accepted")
        void testCompleteOrderWrongStatus() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(BUYER_ID);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.completeOrder(orderId));
            assertEquals("订单状态错误", ex.getMessage());
        }
    }

    // ==================== 订单列表 ====================

    @Nested
    @DisplayName("订单列表")
    class ListOrdersTests {

        @Test
        @DisplayName("我买到的")
        void testListBuyOrders() {
            createOrderByBuyer();

            BaseContext.setCurrentId(BUYER_ID);
            OrderQueryDTO query = new OrderQueryDTO();
            query.setType(OrderConstant.TYPE_BUY);

            PageResult result = orderService.listOrders(query);

            assertNotNull(result);
            assertTrue(result.getTotal() >= 1);
            assertFalse(result.getRecords().isEmpty());

            List<?> records = result.getRecords();
            OrderVO vo = (OrderVO) records.get(0);
            assertEquals(BUYER_ID, vo.getBuyerId());
        }

        @Test
        @DisplayName("我卖出的")
        void testListSellOrders() {
            createOrderByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            OrderQueryDTO query = new OrderQueryDTO();
            query.setType(OrderConstant.TYPE_SELL);

            PageResult result = orderService.listOrders(query);

            assertNotNull(result);
            assertTrue(result.getTotal() >= 1);

            OrderVO vo = (OrderVO) result.getRecords().get(0);
            assertEquals(SELLER_ID, vo.getSellerId());
        }

        @Test
        @DisplayName("按状态筛选")
        void testListOrdersByStatus() {
            Long orderId = createOrderByBuyer();
            BaseContext.setCurrentId(SELLER_ID);
            orderService.acceptOrder(orderId);

            BaseContext.setCurrentId(BUYER_ID);
            OrderQueryDTO query = new OrderQueryDTO();
            query.setType(OrderConstant.TYPE_BUY);
            query.setStatus(OrderConstant.STATUS_ACCEPTED);

            PageResult result = orderService.listOrders(query);

            assertTrue(result.getTotal() >= 1);
            OrderVO vo = (OrderVO) result.getRecords().get(0);
            assertEquals(OrderConstant.STATUS_ACCEPTED, vo.getStatus());
        }
    }

    // ==================== 订单详情 ====================

    @Nested
    @DisplayName("订单详情")
    class GetOrderDetailTests {

        @Test
        @DisplayName("买家查看成功")
        void testGetOrderDetailByBuyer() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(BUYER_ID);
            OrderVO vo = orderService.getOrderDetail(orderId);

            assertNotNull(vo);
            assertEquals(orderId, vo.getId());
            assertEquals(BUYER_ID, vo.getBuyerId());
            assertEquals(SELLER_ID, vo.getSellerId());
            assertNotNull(vo.getProductTitle());
        }

        @Test
        @DisplayName("卖家查看成功")
        void testGetOrderDetailBySeller() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            OrderVO vo = orderService.getOrderDetail(orderId);

            assertNotNull(vo);
            assertEquals(orderId, vo.getId());
        }

        @Test
        @DisplayName("失败 - 无关用户查看")
        void testGetOrderDetailNoPermission() {
            Long orderId = createOrderByBuyer();

            BaseContext.setCurrentId(99999L);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.getOrderDetail(orderId));
            assertEquals("无权操作该订单", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 订单不存在")
        void testGetOrderDetailNotFound() {
            BaseContext.setCurrentId(BUYER_ID);

            BaseException ex = assertThrows(BaseException.class,
                    () -> orderService.getOrderDetail(999999L));
            assertEquals("订单不存在", ex.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private Long createOrderByBuyer() {
        BaseContext.setCurrentId(BUYER_ID);
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setProductId(PRODUCT_ID);
        dto.setRemark("测试订单");
        return orderService.createOrder(dto);
    }

    private Long createAcceptedOrder() {
        Long orderId = createOrderByBuyer();
        BaseContext.setCurrentId(SELLER_ID);
        orderService.acceptOrder(orderId);
        return orderId;
    }
}