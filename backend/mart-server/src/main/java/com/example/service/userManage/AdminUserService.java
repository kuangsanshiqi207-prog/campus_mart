package com.example.service.userManage;

import com.example.dto.userManage.AdminUserQueryDTO;
import com.example.dto.userManage.CertificationAuditDTO;
import com.example.dto.userManage.CreditAdjustDTO;
import com.example.dto.userManage.UserStatusDTO;
import com.example.result.PageResult;
import com.example.vo.userManage.UserDetailVO;
import com.example.vo.user.CertificationVO;
import com.example.vo.user.UserVO;

public interface AdminUserService {

    PageResult listUsers(AdminUserQueryDTO query);

    UserDetailVO getUserDetail(Long id);

    void updateUserStatus(Long id, UserStatusDTO dto);

    void adjustCredit(Long id, CreditAdjustDTO dto);

    PageResult listCertifications(String status, Integer pageNum, Integer pageSize);

    void auditCertification(Long id, CertificationAuditDTO dto);
}