package com.example.service.report.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.MessageConstant;
import com.example.constant.ReportConstant;
import com.example.context.BaseContext;
import com.example.dto.report.AppealCreateDTO;
import com.example.dto.report.ReportCreateDTO;
import com.example.entity.Appeal;
import com.example.entity.File;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.entity.Report;
import com.example.exception.BaseException;
import com.example.mapper.order.OrderMapper;
import com.example.mapper.product.ProductMapper;
import com.example.mapper.report.AppealMapper;
import com.example.mapper.report.ReportMapper;
import com.example.mapper.user.UserMapper;
import com.example.result.PageResult;
import com.example.service.common.FileService;
import com.example.service.report.ReportService;
import com.example.vo.report.ReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final AppealMapper appealMapper;
    private final FileService fileService;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    // ==================== 提交举报 ====================

    @Override
    @Transactional
    public Long createReport(ReportCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 1. 校验举报对象存在，并获取被举报人ID
        Long reportedUserId = validateTarget(dto.getTargetType(), dto.getTargetId(), userId);

        // 2. 不能举报自己
        if (reportedUserId != null && reportedUserId.equals(userId)) {
            throw new BaseException(MessageConstant.REPORT_CANNOT_REPORT_SELF);
        }

        // 3. 不能重复举报（同一人 + 同一对象）
        Report existing = reportMapper.getByReporterAndTarget(
                userId, dto.getTargetType(), dto.getTargetId());
        if (existing != null) {
            throw new BaseException(MessageConstant.REPORT_ALREADY_EXISTS);
        }

        // 4. 处理图片
        String imagesStr = null;
        if (dto.getImageFileIds() != null && !dto.getImageFileIds().isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (Long fileId : dto.getImageFileIds()) {
                File file = fileService.getFileOwnedByUser(fileId, userId);
                urls.add(file.getUrl());
                fileService.markUsed(fileId);
            }
            imagesStr = String.join(",", urls);
        }

        // 5. 构造 Report
        Report report = Report.builder()
                .id(IdUtil.getSnowflakeNextId())
                .reporterId(userId)
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .reason(dto.getReason())
                .description(dto.getDescription())
                .images(imagesStr)
                .status(ReportConstant.STATUS_PENDING)
                .createTime(LocalDateTime.now())
                .build();

        reportMapper.insert(report);

        return report.getId();
    }

    // ==================== 我的举报 ====================

    @Override
    public PageResult listMyReports(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        pageNum = normalizePageNum(pageNum);
        pageSize = normalizePageSize(pageSize);

        int offset = (pageNum - 1) * pageSize;
        Long total = reportMapper.countByReporter(userId);
        List<ReportVO> list = reportMapper.listByReporter(userId, offset, pageSize);

        return new PageResult(total, list);
    }

    // ==================== 举报详情 ====================

    @Override
    public ReportVO getReportDetail(Long id) {
        Long userId = BaseContext.getCurrentId();
        Report report = reportMapper.getById(id);
        if (report == null) {
            throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
        }
        if (!report.getReporterId().equals(userId)) {
            throw new BaseException(MessageConstant.REPORT_NO_PERMISSION);
        }
        return reportMapper.getReportVOById(id);
    }

    // ==================== 提交申诉 ====================

    @Override
    @Transactional
    public Long createAppeal(Long reportId, AppealCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 1. 查举报
        Report report = reportMapper.getById(reportId);
        if (report == null) {
            throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
        }

        // 2. 举报必须已处理（valid 或 invalid）
        if (ReportConstant.STATUS_PENDING.equals(report.getStatus())) {
            throw new BaseException(MessageConstant.APPEAL_REPORT_NOT_HANDLED);
        }

        // 3. 只有被举报人能申诉
        Long reportedUserId = getReportedUserId(report);
        if (reportedUserId == null || !reportedUserId.equals(userId)) {
            throw new BaseException(MessageConstant.APPEAL_ONLY_REPORTED_CAN);
        }

        // 4. 不能重复申诉
        Appeal existing = appealMapper.getByReportId(reportId);
        if (existing != null) {
            throw new BaseException(MessageConstant.APPEAL_ALREADY_EXISTS);
        }

        // 5. 处理图片
        String imagesStr = null;
        if (dto.getImageFileIds() != null && !dto.getImageFileIds().isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (Long fileId : dto.getImageFileIds()) {
                File file = fileService.getFileOwnedByUser(fileId, userId);
                urls.add(file.getUrl());
                fileService.markUsed(fileId);
            }
            imagesStr = String.join(",", urls);
        }

        // 6. 构造 Appeal
        Appeal appeal = Appeal.builder()
                .id(IdUtil.getSnowflakeNextId())
                .reportId(reportId)
                .userId(userId)
                .reason(dto.getReason())
                .images(imagesStr)
                .status(ReportConstant.APPEAL_PENDING)
                .createTime(LocalDateTime.now())
                .build();

        appealMapper.insert(appeal);

        return appeal.getId();
    }

    // ==================== 私有方法 ====================

    /**
     * 校验举报对象存在，返回被举报人的 userId
     */
    private Long validateTarget(String targetType, Long targetId, Long currentUserId) {
        switch (targetType) {
            case ReportConstant.TARGET_PRODUCT:
                Product product = productMapper.getById(targetId);
                if (product == null) {
                    throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
                }
                return product.getSellerId();

            case ReportConstant.TARGET_USER:
                if (userMapper.getById(targetId) == null) {
                    throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
                }
                return targetId;

            case ReportConstant.TARGET_ORDER:
                Order order = orderMapper.getById(targetId);
                if (order == null) {
                    throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
                }
                // 订单被举报人：如果是买家举报，被举报人是卖家；反之亦然
                return order.getBuyerId().equals(currentUserId)
                        ? order.getSellerId()
                        : order.getBuyerId();

            default:
                throw new BaseException("不支持的举报类型");
        }
    }

    /**
     * 从举报记录中获取被举报人ID
     */
    private Long getReportedUserId(Report report) {
        try {
            return validateTarget(report.getTargetType(), report.getTargetId(),
                    report.getReporterId());
        } catch (Exception e) {
            return null;
        }
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