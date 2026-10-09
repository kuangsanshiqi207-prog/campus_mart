package com.example.service.review.impl;

import cn.hutool.core.util.IdUtil;
import com.example.api.file.FileFeignClient;
import com.example.api.message.MessageFeignClient;
import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.constant.OrderConstant;
import com.example.context.BaseContext;
import com.example.dto.review.ReviewCreateDTO;
import com.example.dto.review.ReviewReplyDTO;
import com.example.entity.Order;
import com.example.entity.Review;
import com.example.exception.BaseException;
import com.example.mapper.order.OrderMapper;
import com.example.mapper.review.ReviewMapper;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.review.ReviewService;
import com.example.vo.common.FileVO;
import com.example.vo.review.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final FileFeignClient fileFeignClient;              // ← 改
    private final MessageFeignClient messageFeignClient;        // ← 改

    // ==================== 发表评价 ====================

    @Override
    @Transactional
    public Long createReview(ReviewCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        Order order = orderMapper.getById(dto.getOrderId());
        if (order == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }

        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BaseException(MessageConstant.ORDER_NO_PERMISSION);
        }

        if (!OrderConstant.STATUS_COMPLETED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.REVIEW_ORDER_NOT_COMPLETED);
        }

        Review existing = reviewMapper.getByOrderAndFrom(dto.getOrderId(), userId);
        if (existing != null) {
            throw new BaseException(MessageConstant.REVIEW_ALREADY_EXISTS);
        }

        Long toUserId = isBuyer ? order.getSellerId() : order.getBuyerId();

        // 处理图片（通过 Feign）
        String imagesStr = null;
        if (dto.getImageFileIds() != null && !dto.getImageFileIds().isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (Long fileId : dto.getImageFileIds()) {
                Result<FileVO> fr = fileFeignClient.getFileOwnedByUser(fileId, userId);
                if (fr == null || fr.getCode() != 1 || fr.getData() == null) {
                    throw new BaseException("无权使用该文件");
                }
                urls.add(fr.getData().getUrl());
                fileFeignClient.markUsed(fileId);
            }
            imagesStr = String.join(",", urls);
        }

        Review review = Review.builder()
                .id(IdUtil.getSnowflakeNextId())
                .orderId(dto.getOrderId())
                .productId(order.getProductId())
                .fromUserId(userId)
                .toUserId(toUserId)
                .score(dto.getScore())
                .content(dto.getContent())
                .images(imagesStr)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        reviewMapper.insert(review);

        // 通知被评价人（通过 Feign）
        messageFeignClient.send(
                toUserId,
                NotificationConstant.TYPE_ORDER,
                NotificationConstant.ORDER_REVIEWED_TITLE,
                String.format(NotificationConstant.ORDER_REVIEWED_CONTENT, order.getOrderNo()),
                review.getId()
        );

        return review.getId();
    }

    // ==================== 回复评价 ====================

    @Override
    @Transactional
    public void replyReview(Long id, ReviewReplyDTO dto) {
        Long userId = BaseContext.getCurrentId();

        Review review = reviewMapper.getById(id);
        if (review == null) {
            throw new BaseException(MessageConstant.REVIEW_NOT_FOUND);
        }

        if (!review.getToUserId().equals(userId)) {
            throw new BaseException(MessageConstant.REVIEW_ONLY_RECEIVER_CAN_REPLY);
        }

        if (StringUtils.hasText(review.getReply())) {
            throw new BaseException(MessageConstant.REVIEW_REPLY_ALREADY_EXISTS);
        }

        reviewMapper.updateReply(id, dto.getContent());
    }

    // ==================== 商品评价列表 ====================

    @Override
    public PageResult listByProduct(Long productId, Integer pageNum, Integer pageSize) {
        pageNum = normalizePageNum(pageNum);
        pageSize = normalizePageSize(pageSize);

        int offset = (pageNum - 1) * pageSize;
        Long total = reviewMapper.countByProduct(productId);
        List<ReviewVO> list = reviewMapper.listByProduct(productId, offset, pageSize);

        return new PageResult(total, list);
    }

    // ==================== 我发出的评价 ====================

    @Override
    public PageResult listMyReviews(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        pageNum = normalizePageNum(pageNum);
        pageSize = normalizePageSize(pageSize);

        int offset = (pageNum - 1) * pageSize;
        Long total = reviewMapper.countByFromUser(userId);
        List<ReviewVO> list = reviewMapper.listByFromUser(userId, offset, pageSize);

        return new PageResult(total, list);
    }

    // ==================== 我收到的评价 ====================

    @Override
    public PageResult listReceivedReviews(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        pageNum = normalizePageNum(pageNum);
        pageSize = normalizePageSize(pageSize);

        int offset = (pageNum - 1) * pageSize;
        Long total = reviewMapper.countByToUser(userId);
        List<ReviewVO> list = reviewMapper.listByToUser(userId, offset, pageSize);

        return new PageResult(total, list);
    }

    // ==================== 评价详情 ====================

    @Override
    public ReviewVO getReviewDetail(Long id) {
        ReviewVO vo = reviewMapper.getReviewVOById(id);
        if (vo == null) {
            throw new BaseException(MessageConstant.REVIEW_NOT_FOUND);
        }
        return vo;
    }

    // ==================== 私有方法 ====================

    private Integer normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) return 1;
        return pageNum;
    }

    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1 || pageSize > 100) return 10;
        return pageSize;
    }
}