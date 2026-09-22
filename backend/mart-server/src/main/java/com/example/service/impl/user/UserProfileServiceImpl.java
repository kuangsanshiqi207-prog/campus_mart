package com.example.service.impl.user;

import cn.hutool.core.util.IdUtil;
import com.example.constant.MessageConstant;
import com.example.constant.RedisKeyConstant;
import com.example.constant.UserConstant;
import com.example.context.BaseContext;
import com.example.dto.user.AddressDTO;
import com.example.dto.user.CertificationDTO;
import com.example.dto.user.PasswordUpdateDTO;
import com.example.dto.user.UserUpdateDTO;
import com.example.entity.Address;
import com.example.entity.User;
import com.example.entity.UserCertification;
import com.example.exception.BaseException;
import com.example.mapper.user.AddressMapper;
import com.example.mapper.user.UserCertificationMapper;
import com.example.mapper.user.UserMapper;
import com.example.properties.JwtProperties;
import com.example.service.user.UserProfileService;
import com.example.utils.JwtUtil;
import com.example.vo.user.AddressVO;
import com.example.vo.user.CertificationVO;
import com.example.vo.user.UserVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMapper userMapper;
    private final UserCertificationMapper certificationMapper;
    private final AddressMapper addressMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    // ==================== 个人资料 ====================

    @Override
    public UserVO getProfile() {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    @Transactional
    public void updateProfile(UserUpdateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        User user = User.builder()
                .id(userId)
                .nickname(dto.getNickname())
                .avatar(dto.getAvatar())
                .email(dto.getEmail())
                .build();

        userMapper.updateProfile(user);
    }

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateDTO dto, String token) {
        Long userId = BaseContext.getCurrentId();

        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        // 1. 校验原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BaseException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 2. 新密码不能和原密码相同
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_SAME);
        }

        // 3. 更新密码
        userMapper.updatePassword(userId, passwordEncoder.encode(dto.getNewPassword()));

        // 4. 当前 token 加入黑名单，强制重新登录
        addTokenToBlacklist(token);
    }

    // ==================== 校园认证 ====================

    @Override
    @Transactional
    public Long submitCertification(CertificationDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 查最新一条认证记录
        List<UserCertification> list = certificationMapper.listByUserId(userId);
        if (!list.isEmpty()) {
            UserCertification latest = list.get(0);
            if (UserConstant.CERT_STATUS_PENDING.equals(latest.getStatus())) {
                throw new BaseException(MessageConstant.CERTIFICATION_PENDING);
            }
            if (UserConstant.CERT_STATUS_APPROVED.equals(latest.getStatus())) {
                throw new BaseException(MessageConstant.CERTIFICATION_APPROVED);
            }
        }

        UserCertification cert = UserCertification.builder()
                .id(IdUtil.getSnowflakeNextId())
                .userId(userId)
                .school(dto.getSchool())
                .studentNo(dto.getStudentNo())
                .realName(dto.getRealName())
                .cardImage(dto.getCardImage())
                .status(UserConstant.CERT_STATUS_PENDING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        certificationMapper.insert(cert);
        return cert.getId();
    }

    @Override
    public CertificationVO getCertification() {
        Long userId = BaseContext.getCurrentId();

        List<UserCertification> list = certificationMapper.listByUserId(userId);
        if (list.isEmpty()) {
            return null;
        }

        UserCertification cert = list.get(0);
        CertificationVO vo = new CertificationVO();
        BeanUtils.copyProperties(cert, vo);
        return vo;
    }

    // ==================== 地址管理 ====================

    @Override
    public List<AddressVO> listAddresses() {
        Long userId = BaseContext.getCurrentId();

        return addressMapper.listByUserId(userId).stream()
                .map(this::toAddressVO)
                .toList();
    }

    @Override
    @Transactional
    public Long addAddress(AddressDTO dto) {
        Long userId = BaseContext.getCurrentId();

        Integer isDefault = dto.getIsDefault() == null
                ? UserConstant.ADDRESS_NOT_DEFAULT
                : dto.getIsDefault();

        // 如果设为默认，先清空其他默认
        if (UserConstant.ADDRESS_DEFAULT.equals(isDefault)) {
            addressMapper.clearDefault(userId);
        }

        Address address = Address.builder()
                .id(IdUtil.getSnowflakeNextId())
                .userId(userId)
                .receiver(dto.getReceiver())
                .phone(dto.getPhone())
                .region(dto.getRegion())
                .detail(dto.getDetail())
                .isDefault(isDefault)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        addressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional
    public void updateAddress(Long id, AddressDTO dto) {
        Long userId = BaseContext.getCurrentId();

        Address existing = addressMapper.getById(id);
        if (existing == null) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_NO_PERMISSION);
        }

        Integer isDefault = dto.getIsDefault() == null
                ? UserConstant.ADDRESS_NOT_DEFAULT
                : dto.getIsDefault();

        if (UserConstant.ADDRESS_DEFAULT.equals(isDefault)) {
            addressMapper.clearDefault(userId);
        }

        Address address = Address.builder()
                .id(id)
                .receiver(dto.getReceiver())
                .phone(dto.getPhone())
                .region(dto.getRegion())
                .detail(dto.getDetail())
                .isDefault(isDefault)
                .build();

        addressMapper.update(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        Long userId = BaseContext.getCurrentId();

        Address existing = addressMapper.getById(id);
        if (existing == null) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_NO_PERMISSION);
        }

        addressMapper.deleteById(id);
    }

    // ==================== 私有方法 ====================

    private AddressVO toAddressVO(Address address) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }

    private void addTokenToBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Claims claims;
        try {
            claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
        } catch (Exception e) {
            log.warn("修改密码后加黑名单失败：{}", e.getMessage());
            return;
        }

        Date expiration = claims.getExpiration();
        long ttlMillis = expiration.getTime() - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            return;
        }

        String key = String.format(RedisKeyConstant.TOKEN_BLACKLIST, token);
        redisTemplate.opsForValue().set(key, "1", ttlMillis, TimeUnit.MILLISECONDS);

        log.info("修改密码成功，当前 token 已加入黑名单，剩余有效期 {} 毫秒", ttlMillis);
    }
}