package com.example.service.reportManage.impl;

import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.constant.ReportConstant;
import com.example.context.BaseContext;
import com.example.dto.reportManage.AdminAppealQueryDTO;
import com.example.dto.reportManage.AdminReportQueryDTO;
import com.example.dto.reportManage.AppealHandleDTO;
import com.example.dto.reportManage.ReportHandleDTO;
import com.example.entity.Appeal;
import com.example.entity.Product;
import com.example.entity.Report;
import com.example.entity.User;
import com.example.exception.BaseException;
import com.example.mapper.product.ProductMapper;
import com.example.mapper.report.AppealMapper;
import com.example.mapper.report.ReportMapper;
import com.example.mapper.user.UserMapper;
import com.example.result.PageResult;
import com.example.service.message.MessageSender;
import com.example.service.reportManage.AdminReportService;
import com.example.vo.report.AppealVO;
import com.example.vo.report.ReportVO;
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
public class AdminReportServiceImpl implements AdminReportService {

    private final ReportMapper reportMapper;
    private final AppealMapper appealMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final MessageSender messageSender;

    // ==================== 举报列表 ====================

    @Override
    public PageResult listReports(AdminReportQueryDTO query) {
        query.setPageNum(normalizePageNum(query.getPageNum()));
        query.setPageSize(normalizePageSize(query.getPageSize()));

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        Long total = reportMapper.countAdmin(query);
        List<Report> list = reportMapper.listAdmin(query, offset, query.getPageSize());

        List<ReportVO> voList = list.stream()
                .map(this::toReportVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    // ==================== 举报详情 ====================

    @Override
    public ReportVO getReportDetail(Long id) {
        Report report = reportMapper.getById(id);
        if (report == null) {
            throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
        }
        return toReportVO(report);
    }

    // ==================== 处理举报 ====================

    @Override
    @Transactional
    public void handleReport(Long id, ReportHandleDTO dto) {
        Report report = reportMapper.getById(id);
        if (report == null) {
            throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
        }

        if (!ReportConstant.STATUS_PENDING.equals(report.getStatus())) {
            throw new BaseException(MessageConstant.REPORT_ALREADY_HANDLED);
        }

        String result = dto.getResult();
        if (!ReportConstant.STATUS_VALID.equals(result)
                && !ReportConstant.STATUS_INVALID.equals(result)) {
            throw new BaseException(MessageConstant.REPORT_HANDLE_RESULT_INVALID);
        }

        Long adminId = BaseContext.getCurrentId();

        // 处理动作：举报不成立时设为 none
        String action = ReportConstant.STATUS_VALID.equals(result)
                ? (dto.getAction() != null ? dto.getAction() : ReportConstant.ACTION_NONE)
                : ReportConstant.ACTION_NONE;

        reportMapper.handle(id, result, action, dto.getReason(), adminId);

        // 举报成立时执行处罚动作
        if (ReportConstant.STATUS_VALID.equals(result)) {
            executeAction(report, action, dto.getReason());
        }

        // 通知举报人处理结果
        messageSender.send(report.getReporterId(),
                NotificationConstant.TYPE_REPORT,
                "举报已处理",
                "您提交的举报已处理，结果：" +
                        (ReportConstant.STATUS_VALID.equals(result) ? "举报成立" : "举报不成立"),
                id);
    }

    // ==================== 申诉列表 ====================

    @Override
    public PageResult listAppeals(AdminAppealQueryDTO query) {
        query.setPageNum(normalizePageNum(query.getPageNum()));
        query.setPageSize(normalizePageSize(query.getPageSize()));

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        Long total = appealMapper.countAdmin(query);
        List<Appeal> list = appealMapper.listAdmin(query, offset, query.getPageSize());

        List<AppealVO> voList = list.stream()
                .map(this::toAppealVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    // ==================== 处理申诉 ====================

    @Override
    @Transactional
    public void handleAppeal(Long id, AppealHandleDTO dto) {
        Appeal appeal = appealMapper.getById(id);
        if (appeal == null) {
            throw new BaseException(MessageConstant.APPEAL_NOT_FOUND);
        }

        if (!ReportConstant.APPEAL_PENDING.equals(appeal.getStatus())) {
            throw new BaseException(MessageConstant.APPEAL_ALREADY_HANDLED);
        }

        String result = dto.getResult();
        String status;
        if ("approve".equals(result)) {
            status = ReportConstant.APPEAL_APPROVED;
        } else if ("reject".equals(result)) {
            status = ReportConstant.APPEAL_REJECTED;
        } else {
            throw new BaseException(MessageConstant.APPEAL_HANDLE_RESULT_INVALID);
        }

        Long adminId = BaseContext.getCurrentId();
        appealMapper.handle(id, status, dto.getReason(), adminId);

        // 通知申诉人
        String resultText = ReportConstant.APPEAL_APPROVED.equals(status)
                ? "申诉通过，处罚已撤销"
                : "申诉被驳回，维持处罚";
        messageSender.send(appeal.getUserId(),
                NotificationConstant.TYPE_REPORT,
                "申诉已处理",
                "您的申诉已处理：" + resultText,
                appeal.getReportId());
    }

    // ==================== 私有方法 ====================

    /**
     * 执行处罚动作
     */
    private void executeAction(Report report, String action, String reason) {
        if (ReportConstant.ACTION_OFFLINE.equals(action)) {
            // 下架商品
            if ("product".equals(report.getTargetType())) {
                productMapper.updateStatus(report.getTargetId(), "offline");
                Product product = productMapper.getById(report.getTargetId());
                if (product != null) {
                    messageSender.send(product.getSellerId(),
                            NotificationConstant.TYPE_SYSTEM,
                            "商品已被下架",
                            "您的商品因违规举报被下架，原因：" + reason,
                            product.getId());
                }
            }
        } else if (ReportConstant.ACTION_BAN.equals(action)) {
            // 封禁用户
            Long targetUserId = resolveTargetUserId(report);
            if (targetUserId != null) {
                userMapper.updateStatus(targetUserId, "banned");
                messageSender.send(targetUserId,
                        NotificationConstant.TYPE_SYSTEM,
                        "账号已被封禁",
                        "您的账号因违规被封禁，原因：" + reason,
                        report.getId());
            }
        } else if (ReportConstant.ACTION_WARN.equals(action)) {
            // 警告用户
            Long targetUserId = resolveTargetUserId(report);
            if (targetUserId != null) {
                messageSender.send(targetUserId,
                        NotificationConstant.TYPE_SYSTEM,
                        "违规警告",
                        "您因违规收到警告，原因：" + reason,
                        report.getId());
            }
        }
    }

    /**
     * 根据举报对象类型解析被举报人ID
     */
    private Long resolveTargetUserId(Report report) {
        String type = report.getTargetType();
        if ("user".equals(type)) {
            return report.getTargetId();
        } else if ("product".equals(type)) {
            Product product = productMapper.getById(report.getTargetId());
            return product != null ? product.getSellerId() : null;
        }
        // order 类型暂不处理
        return null;
    }

    private ReportVO toReportVO(Report report) {
        ReportVO vo = new ReportVO();
        BeanUtils.copyProperties(report, vo);

        User reporter = userMapper.getById(report.getReporterId());
        if (reporter != null) {
            vo.setReporterNickname(reporter.getNickname());
        }
        return vo;
    }

    private AppealVO toAppealVO(Appeal appeal) {
        AppealVO vo = new AppealVO();
        BeanUtils.copyProperties(appeal, vo);

        User user = userMapper.getById(appeal.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
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