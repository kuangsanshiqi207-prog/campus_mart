package com.example.service.userManage.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.constant.ReportConstant;
import com.example.constant.UserConstant;
import com.example.context.BaseContext;
import com.example.dto.userManage.AdminUserQueryDTO;
import com.example.dto.userManage.CertificationAuditDTO;
import com.example.dto.userManage.CreditAdjustDTO;
import com.example.dto.userManage.UserStatusDTO;
import com.example.entity.CreditLog;
import com.example.entity.User;
import com.example.entity.UserCertification;
import com.example.exception.BaseException;
import com.example.mapper.stats.CreditLogMapper;
import com.example.mapper.user.UserCertificationMapper;
import com.example.mapper.user.UserMapper;
import com.example.result.PageResult;
import com.example.service.userManage.AdminUserService;
import com.example.service.message.MessageSender;
import com.example.vo.userManage.UserDetailVO;
import com.example.vo.user.CertificationVO;
import com.example.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final UserCertificationMapper certificationMapper;
    private final CreditLogMapper creditLogMapper;
    private final MessageSender messageSender;

    // ==================== 用户列表 ====================

    @Override
    public PageResult listUsers(AdminUserQueryDTO query) {
        query.setPageNum(normalizePageNum(query.getPageNum()));
        query.setPageSize(normalizePageSize(query.getPageSize()));

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        Long total = userMapper.countUsers(query);
        List<User> list = userMapper.listUsers(query, offset, query.getPageSize());

        List<UserVO> voList = list.stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    // ==================== 用户详情 ====================

    @Override
    public UserDetailVO getUserDetail(Long id) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        UserVO userVO = toUserVO(user);

        CertificationVO certVO = null;
        List<UserCertification> certs = certificationMapper.listByUserId(id);
        if (!certs.isEmpty()) {
            certVO = new CertificationVO();
            BeanUtils.copyProperties(certs.get(0), certVO);
        }

        return UserDetailVO.builder()
                .user(userVO)
                .certification(certVO)
                .build();
    }

    // ==================== 封禁/解封 ====================

    @Override
    @Transactional
    public void updateUserStatus(Long id, UserStatusDTO dto) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        if (!"normal".equals(dto.getStatus()) && !"banned".equals(dto.getStatus())) {
            throw new BaseException("状态值不合法");
        }

        userMapper.updateStatus(id, dto.getStatus());

        // 通知用户
        String title = "banned".equals(dto.getStatus()) ? "账号已被封禁" : "账号已解封";
        String content = "banned".equals(dto.getStatus())
                ? "您的账号已被封禁，原因：" + dto.getReason()
                : "您的账号已恢复正常。";
        messageSender.send(id, NotificationConstant.TYPE_SYSTEM, title, content, null);
    }

    // ==================== 调整信用分 ====================

    @Override
    @Transactional
    public void adjustCredit(Long id, CreditAdjustDTO dto) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        int before = user.getCreditScore();
        int after = Math.max(0, before + dto.getDelta());

        userMapper.updateCreditScore(id, after);

        // 写信用分流水
        CreditLog creditLog = CreditLog.builder()
                .id(IdUtil.getSnowflakeNextId())
                .userId(id)
                .delta(dto.getDelta())
                .beforeScore(before)
                .afterScore(after)
                .reason(dto.getReason())
                .bizType("admin")
                .bizId(BaseContext.getCurrentId())
                .createTime(LocalDateTime.now())
                .build();
        creditLogMapper.insert(creditLog);

        // 通知用户
        String title = dto.getDelta() > 0 ? "信用分增加" : "信用分减少";
        String content = String.format("您的信用分%s %d 分，当前信用分 %d。原因：%s",
                dto.getDelta() > 0 ? "增加" : "减少",
                Math.abs(dto.getDelta()), after, dto.getReason());
        messageSender.send(id, NotificationConstant.TYPE_SYSTEM, title, content, null);
    }

    // ==================== 认证审核列表 ====================

    @Override
    public PageResult listCertifications(String status, Integer pageNum, Integer pageSize) {
        pageNum = normalizePageNum(pageNum);
        pageSize = normalizePageSize(pageSize);

        int offset = (pageNum - 1) * pageSize;
        Long total = certificationMapper.countByStatus(status);
        List<UserCertification> list = certificationMapper.listByStatus(status, offset, pageSize);

        List<CertificationVO> voList = list.stream()
                .map(this::toCertificationVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    // ==================== 审核认证 ====================

    @Override
    @Transactional
    public void auditCertification(Long id, CertificationAuditDTO dto) {
        UserCertification cert = certificationMapper.getById(id);
        if (cert == null) {
            throw new BaseException(MessageConstant.CERTIFICATION_NOT_FOUND);
        }

        if (!UserConstant.CERT_STATUS_PENDING.equals(cert.getStatus())) {
            throw new BaseException("该认证已审核过");
        }

        if (!UserConstant.CERT_STATUS_APPROVED.equals(dto.getStatus())
                && !UserConstant.CERT_STATUS_REJECTED.equals(dto.getStatus())) {
            throw new BaseException("审核结果不合法");
        }

        certificationMapper.audit(id, dto.getStatus(), dto.getReason(), BaseContext.getCurrentId());

        if (UserConstant.CERT_STATUS_APPROVED.equals(dto.getStatus())) {
            // 通过则更新用户认证状态
            userMapper.updateCertified(cert.getUserId(), UserConstant.CERTIFIED);

            messageSender.send(cert.getUserId(),
                    NotificationConstant.TYPE_AUDIT,
                    NotificationConstant.AUDIT_CERT_APPROVED_TITLE,
                    NotificationConstant.AUDIT_CERT_APPROVED_CONTENT,
                    id);
        } else {
            messageSender.send(cert.getUserId(),
                    NotificationConstant.TYPE_AUDIT,
                    NotificationConstant.AUDIT_CERT_REJECTED_TITLE,
                    String.format(NotificationConstant.AUDIT_CERT_REJECTED_CONTENT, dto.getReason()),
                    id);
        }
    }

    // ==================== 私有方法 ====================

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private CertificationVO toCertificationVO(UserCertification cert) {
        CertificationVO vo = new CertificationVO();
        BeanUtils.copyProperties(cert, vo);
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