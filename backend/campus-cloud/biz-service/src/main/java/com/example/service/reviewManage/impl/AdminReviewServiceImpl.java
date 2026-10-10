package com.example.service.reviewManage.impl;

import com.example.api.ai.AiFeignClient;
import com.example.api.message.MessageFeignClient;
import com.example.api.user.UserFeignClient;
import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.dto.ai.AiAuditDTO;
import com.example.dto.reviewManage.AdminReviewQueryDTO;
import com.example.entity.Review;
import com.example.exception.BaseException;
import com.example.mapper.review.ReviewMapper;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.reviewManage.AdminReviewService;
import com.example.vo.ai.AiAuditVO;
import com.example.vo.review.ReviewVO;
import com.example.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewMapper reviewMapper;
    private final UserFeignClient userFeignClient;              // ← 改
    private final MessageFeignClient messageFeignClient;        // ← 改
    private final AiFeignClient aiFeignClient;

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

        // 通知评价发起人（通过 Feign）
        messageFeignClient.send(review.getFromUserId(),
                NotificationConstant.TYPE_SYSTEM,
                "评价已被删除",
                "您发表的评价已被管理员删除。",
                id);
    }

    // ==================== AI 辅助审核 ====================

    @Override
    public AiAuditVO aiAudit(Long id) {
        Review review = reviewMapper.getById(id);
        if (review == null) {
            throw new BaseException(MessageConstant.REVIEW_NOT_FOUND);
        }

        AiAuditDTO dto = new AiAuditDTO();
        dto.setAuditType("review");

        Map<String, String> textFields = new HashMap<>();
        putIfPresent(textFields, "content", review.getContent());
        if (review.getScore() != null) {
            textFields.put("score", review.getScore().toString());
        }
        dto.setTextFields(textFields);
        dto.setImageUrls(splitImages(review.getImages()));

        return callAi(dto);
    }

    // ==================== 私有方法 ====================

    private AiAuditVO callAi(AiAuditDTO dto) {
        try {
            Result<AiAuditVO> result = aiFeignClient.audit(dto);
            if (result != null && result.getCode() != null && result.getCode() == 1 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("AI 审核调用失败：{}", e.getMessage());
        }
        return null;
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    private List<String> splitImages(String images) {
        if (images == null || images.isBlank()) {
            return List.of();
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private ReviewVO toReviewVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        // ← 通过 Feign 查评价人昵称
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