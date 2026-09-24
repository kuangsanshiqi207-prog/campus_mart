package com.example.service;

import com.example.constant.OrderConstant;
import com.example.context.BaseContext;
import com.example.dto.order.OrderCreateDTO;
import com.example.dto.review.ReviewCreateDTO;
import com.example.dto.review.ReviewReplyDTO;
import com.example.exception.BaseException;
import com.example.mapper.review.ReviewMapper;
import com.example.result.PageResult;
import com.example.service.order.OrderService;
import com.example.service.review.ReviewService;
import com.example.vo.review.ReviewVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("评价模块 - Service 层测试")
public class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewMapper reviewMapper;

    private static final Long SELLER_ID = 1L;
    private static final Long BUYER_ID = 2L;
    private static final Long PRODUCT_ID = 1001L;

    @AfterEach
    public void tearDown() {
        BaseContext.removeCurrentId();
    }

    // ==================== 发表评价 ====================

    @Nested
    @DisplayName("发表评价")
    class CreateReviewTests {

        @Test
        @DisplayName("买家评价卖家 - 成功")
        void testBuyerCreateReviewSuccess() {
            Long orderId = createCompletedOrder();

            BaseContext.setCurrentId(BUYER_ID);
            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(orderId);
            dto.setScore(5);
            dto.setContent("东西很好，卖家很nice");

            Long reviewId = reviewService.createReview(dto);

            assertNotNull(reviewId);
            ReviewVO vo = reviewService.getReviewDetail(reviewId);
            assertEquals(5, vo.getScore());
            assertEquals(BUYER_ID, vo.getFromUserId());
            assertEquals(SELLER_ID, vo.getToUserId());
        }

        @Test
        @DisplayName("卖家评价买家 - 成功")
        void testSellerCreateReviewSuccess() {
            Long orderId = createCompletedOrder();

            BaseContext.setCurrentId(SELLER_ID);
            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(orderId);
            dto.setScore(5);
            dto.setContent("买家很爽快");

            Long reviewId = reviewService.createReview(dto);

            assertNotNull(reviewId);
            ReviewVO vo = reviewService.getReviewDetail(reviewId);
            assertEquals(SELLER_ID, vo.getFromUserId());
            assertEquals(BUYER_ID, vo.getToUserId());
        }

        @Test
        @DisplayName("失败 - 订单未完成")
        void testCreateReviewOrderNotCompleted() {
            BaseContext.setCurrentId(BUYER_ID);
            OrderCreateDTO orderDTO = new OrderCreateDTO();
            orderDTO.setProductId(PRODUCT_ID);
            Long orderId = orderService.createOrder(orderDTO);

            BaseContext.setCurrentId(BUYER_ID);
            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(orderId);
            dto.setScore(5);

            BaseException ex = assertThrows(BaseException.class,
                    () -> reviewService.createReview(dto));
            assertEquals("订单未完成，不能评价", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 重复评价")
        void testCreateReviewAlreadyExists() {
            Long orderId = createCompletedOrder();

            BaseContext.setCurrentId(BUYER_ID);
            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(orderId);
            dto.setScore(5);

            reviewService.createReview(dto);

            BaseException ex = assertThrows(BaseException.class,
                    () -> reviewService.createReview(dto));
            assertEquals("您已评价过该订单", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 非参与方评价")
        void testCreateReviewNotParticipant() {
            Long orderId = createCompletedOrder();

            BaseContext.setCurrentId(99999L);
            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(orderId);
            dto.setScore(5);

            BaseException ex = assertThrows(BaseException.class,
                    () -> reviewService.createReview(dto));
            assertEquals("无权操作该订单", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 订单不存在")
        void testCreateReviewOrderNotFound() {
            BaseContext.setCurrentId(BUYER_ID);
            ReviewCreateDTO dto = new ReviewCreateDTO();
            dto.setOrderId(999999L);
            dto.setScore(5);

            BaseException ex = assertThrows(BaseException.class,
                    () -> reviewService.createReview(dto));
            assertEquals("订单不存在", ex.getMessage());
        }
    }

    // ==================== 回复评价 ====================

    @Nested
    @DisplayName("回复评价")
    class ReplyReviewTests {

        @Test
        @DisplayName("被评价人回复 - 成功")
        void testReplySuccess() {
            Long reviewId = createReviewByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            ReviewReplyDTO dto = new ReviewReplyDTO();
            dto.setContent("谢谢支持");

            reviewService.replyReview(reviewId, dto);

            ReviewVO vo = reviewService.getReviewDetail(reviewId);
            assertEquals("谢谢支持", vo.getReply());
            assertNotNull(vo.getReplyTime());
        }

        @Test
        @DisplayName("失败 - 非被评价人回复")
        void testReplyNotReceiver() {
            Long reviewId = createReviewByBuyer();

            BaseContext.setCurrentId(BUYER_ID);
            ReviewReplyDTO dto = new ReviewReplyDTO();
            dto.setContent("随便回复");

            BaseException ex = assertThrows(BaseException.class,
                    () -> reviewService.replyReview(reviewId, dto));
            assertEquals("只有被评价人可以回复", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 重复回复")
        void testReplyAlreadyExists() {
            Long reviewId = createReviewByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            ReviewReplyDTO dto = new ReviewReplyDTO();
            dto.setContent("第一次回复");
            reviewService.replyReview(reviewId, dto);

            BaseException ex = assertThrows(BaseException.class,
                    () -> reviewService.replyReview(reviewId, dto));
            assertEquals("该评价已回复过", ex.getMessage());
        }

        @Test
        @DisplayName("失败 - 评价不存在")
        void testReplyNotFound() {
            BaseContext.setCurrentId(SELLER_ID);
            ReviewReplyDTO dto = new ReviewReplyDTO();
            dto.setContent("随便");

            BaseException ex = assertThrows(BaseException.class,
                    () -> reviewService.replyReview(999999L, dto));
            assertEquals("评价不存在", ex.getMessage());
        }
    }

    // ==================== 列表 ====================

    @Nested
    @DisplayName("评价列表")
    class ListTests {

        @Test
        @DisplayName("商品评价列表")
        void testListByProduct() {
            createReviewByBuyer();

            PageResult result = reviewService.listByProduct(PRODUCT_ID, 1, 10);

            assertNotNull(result);
            assertTrue(result.getTotal() >= 1);
            assertFalse(result.getRecords().isEmpty());
        }

        @Test
        @DisplayName("我发出的评价")
        void testListMyReviews() {
            createReviewByBuyer();

            BaseContext.setCurrentId(BUYER_ID);
            PageResult result = reviewService.listMyReviews(1, 10);

            assertNotNull(result);
            assertTrue(result.getTotal() >= 1);
            ReviewVO vo = (ReviewVO) result.getRecords().get(0);
            assertEquals(BUYER_ID, vo.getFromUserId());
        }

        @Test
        @DisplayName("我收到的评价")
        void testListReceived() {
            createReviewByBuyer();

            BaseContext.setCurrentId(SELLER_ID);
            PageResult result = reviewService.listReceivedReviews(1, 10);

            assertNotNull(result);
            assertTrue(result.getTotal() >= 1);
            ReviewVO vo = (ReviewVO) result.getRecords().get(0);
            assertEquals(SELLER_ID, vo.getToUserId());
        }
    }

    // ==================== 详情 ====================

    @Nested
    @DisplayName("评价详情")
    class DetailTests {

        @Test
        @DisplayName("查看成功")
        void testGetDetail() {
            Long reviewId = createReviewByBuyer();

            ReviewVO vo = reviewService.getReviewDetail(reviewId);

            assertNotNull(vo);
            assertEquals(reviewId, vo.getId());
            assertEquals(5, vo.getScore());
        }

        @Test
        @DisplayName("失败 - 不存在")
        void testGetDetailNotFound() {
            BaseException ex = assertThrows(BaseException.class,
                    () -> reviewService.getReviewDetail(999999L));
            assertEquals("评价不存在", ex.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private Long createCompletedOrder() {
        // 买家申请
        BaseContext.setCurrentId(BUYER_ID);
        OrderCreateDTO orderDTO = new OrderCreateDTO();
        orderDTO.setProductId(PRODUCT_ID);
        Long orderId = orderService.createOrder(orderDTO);

        // 卖家接受
        BaseContext.setCurrentId(SELLER_ID);
        orderService.acceptOrder(orderId);

        // 买家完成
        BaseContext.setCurrentId(BUYER_ID);
        orderService.completeOrder(orderId);

        return orderId;
    }

    private Long createReviewByBuyer() {
        Long orderId = createCompletedOrder();
        BaseContext.setCurrentId(BUYER_ID);
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(orderId);
        dto.setScore(5);
        dto.setContent("很好");
        return reviewService.createReview(dto);
    }
}