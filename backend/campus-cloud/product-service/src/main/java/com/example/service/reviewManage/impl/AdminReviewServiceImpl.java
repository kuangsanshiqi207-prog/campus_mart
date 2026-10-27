package com.example.service.reviewManage.impl;

import com.example.api.user.UserFeignClient;
import com.example.dto.reviewManage.AdminReviewQueryDTO;
import com.example.entity.Review;
import com.example.mapper.review.ReviewMapper;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.reviewManage.AdminReviewService;
import com.example.vo.review.ReviewVO;
import com.example.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewMapper reviewMapper;
    private final UserFeignClient userFeignClient;

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

    // ==================== 私有方法 ====================

    private ReviewVO toReviewVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 通过 Feign 查评价人昵称
        Result<UserVO> r = userFeignClient.getById(review.getFromUserId());
        if (r != null && r.getCode() == 1 && r.getData() != null) {
            vo.setFromUserNickname(r.getData().getNickname());
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
