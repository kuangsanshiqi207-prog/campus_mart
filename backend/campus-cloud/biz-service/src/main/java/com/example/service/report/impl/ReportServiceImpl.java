package com.example.service.report.impl;

import cn.hutool.core.util.IdUtil;
import com.example.api.file.FileFeignClient;
import com.example.api.user.UserFeignClient;
import com.example.constant.MessageConstant;
import com.example.constant.ReportConstant;
import com.example.context.BaseContext;
import com.example.dto.report.AppealCreateDTO;
import com.example.dto.report.ReportCreateDTO;
import com.example.entity.Appeal;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.entity.Report;
import com.example.exception.BaseException;
import com.example.mapper.order.OrderMapper;
import com.example.mapper.product.ProductMapper;
import com.example.mapper.report.AppealMapper;
import com.example.mapper.report.ReportMapper;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.report.ReportService;
import com.example.vo.common.FileVO;
import com.example.vo.report.ReportVO;
import com.example.vo.user.UserVO;
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
    private final FileFeignClient fileFeignClient;       // ← 改
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final UserFeignClient userFeignClient;       // ← 改

    // ==================== 提交举报 ====================

    @Override
    @Transactional
    public Long createReport(ReportCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        Long reportedUserId = validateTarget(dto.getTargetType(), dto.getTargetId(), userId);

        if (reportedUserId != null && reportedUserId.equals(userId)) {
            throw new BaseException(MessageConstant.REPORT_CANNOT_REPORT_SELF);
        }

        Report existing = reportMapper.getByReporterAndTarget(
                userId, dto.getTargetType(), dto.getTargetId());
        if (existing != null) {
            throw new BaseException(MessageConstant.REPORT_ALREADY_EXISTS);
        }

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

        Report report = reportMapper.getById(reportId);
        if (report == null) {
            throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
        }

        if (ReportConstant.STATUS_PENDING.equals(report.getStatus())) {
            throw new BaseException(MessageConstant.APPEAL_REPORT_NOT_HANDLED);
        }

        Long reportedUserId = getReportedUserId(report);
        if (reportedUserId == null || !reportedUserId.equals(userId)) {
            throw new BaseException(MessageConstant.APPEAL_ONLY_REPORTED_CAN);
        }

        Appeal existing = appealMapper.getByReportId(reportId);
        if (existing != null) {
            throw new BaseException(MessageConstant.APPEAL_ALREADY_EXISTS);
        }

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

    private Long validateTarget(String targetType, Long targetId, Long currentUserId) {
        switch (targetType) {
            case ReportConstant.TARGET_PRODUCT:
                Product product = productMapper.getById(targetId);
                if (product == null) {
                    throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
                }
                return product.getSellerId();

            case ReportConstant.TARGET_USER:
                // ← 通过 Feign 查用户
                Result<UserVO> ur = userFeignClient.getById(targetId);
                if (ur == null || ur.getCode() != 1 || ur.getData() == null) {
                    throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
                }
                return targetId;

            case ReportConstant.TARGET_ORDER:
                Order order = orderMapper.getById(targetId);
                if (order == null) {
                    throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
                }
                return order.getBuyerId().equals(currentUserId)
                        ? order.getSellerId()
                        : order.getBuyerId();

            default:
                throw new BaseException("不支持的举报类型");
        }
    }

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