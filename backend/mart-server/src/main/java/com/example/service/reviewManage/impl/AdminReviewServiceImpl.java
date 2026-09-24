package com.example.service.reviewManage.impl;

import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.dto.reviewManage.AdminReviewQueryDTO;
import com.example.entity.Review;
import com.example.entity.User;
import com.example.exception.BaseException;
import com.example.mapper.review.ReviewMapper;
import com.example.mapper.user.UserMapper;
import com.example.result.PageResult;
import com.example.service.message.MessageSender;
import com.example.service.reviewManage.AdminReviewService;
import com.example.vo.review.ReviewVO;
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
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final MessageSender messageSender;

    // ==================== 评价列表 ====================

    @Override
    public PageResult listReviews(AdminReviewQueryDTO query) {
        query.setPageNum(normalizePageNum(query.getPageNum()));
        query.setPageSize(normalizePageSize(query.getPageSize()));

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        Long total = reviewMapper.countAdmin(query);
        List<Review> list = reviewMapper.listAdmin(query, offset, query.getPageSize());

        List<ReviewVO> voList = list.stream()
                .map(this::toReviewVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    // ==================== 删除违规评价 ====================

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewMapper.getById(id);
        if (review == null) {
            throw new BaseException(MessageConstant.REVIEW_NOT_FOUND);
        }

        reviewMapper.deleteById(id);

        // 通知评价发起人
        messageSender.send(review.getFromUserId(),
                NotificationConstant.TYPE_SYSTEM,
                "评价已被删除",
                "您发表的评价已被管理员删除。",
                id);
    }

    // ==================== 私有方法 ====================

    private ReviewVO toReviewVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        User fromUser = userMapper.getById(review.getFromUserId());
        if (fromUser != null) {
            vo.setFromUserNickname(fromUser.getNickname());
        }

        return vo;
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