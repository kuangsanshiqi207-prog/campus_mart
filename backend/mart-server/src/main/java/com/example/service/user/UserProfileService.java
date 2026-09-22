package com.example.service.user;

import com.example.dto.user.AddressDTO;
import com.example.dto.user.CertificationDTO;
import com.example.dto.user.PasswordUpdateDTO;
import com.example.dto.user.UserUpdateDTO;
import com.example.vo.user.AddressVO;
import com.example.vo.user.CertificationVO;
import com.example.vo.user.UserVO;

import java.util.List;

public interface UserProfileService {

    // ==================== 个人资料 ====================
    UserVO getProfile();

    void updateProfile(UserUpdateDTO dto);

    void updatePassword(PasswordUpdateDTO dto, String token);

    // ==================== 校园认证 ====================
    Long submitCertification(CertificationDTO dto);

    CertificationVO getCertification();

    // ==================== 地址管理 ====================
    List<AddressVO> listAddresses();

    Long addAddress(AddressDTO dto);

    void updateAddress(Long id, AddressDTO dto);

    void deleteAddress(Long id);
}