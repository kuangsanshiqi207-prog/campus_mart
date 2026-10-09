package com.example.service.admin;

import com.example.dto.admin.AdminLoginDTO;
import com.example.vo.admin.AdminVO;
import com.example.vo.user.LoginVO;

public interface AdminService {

    LoginVO login(AdminLoginDTO dto);

    void logout(String token);

    AdminVO getCurrentAdmin();
}