package com.example.service;

import com.example.constant.RedisKeyConstant;
import com.example.constant.UserConstant;
import com.example.context.BaseContext;
import com.example.dto.user.AddressDTO;
import com.example.dto.user.CertificationDTO;
import com.example.dto.user.LoginDTO;
import com.example.dto.user.PasswordUpdateDTO;
import com.example.dto.user.UserUpdateDTO;
import com.example.exception.BaseException;
import com.example.service.user.UserProfileService;
import com.example.service.user.UserService;
import com.example.vo.user.AddressVO;
import com.example.vo.user.CertificationVO;
import com.example.vo.user.LoginVO;
import com.example.vo.user.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserProfileServiceTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Long TEST_USER_ID = 1L;

    @AfterEach
    void tearDownAndCleanRedis() {
        BaseContext.removeCurrentId();
        Set<String> keys = redisTemplate.keys("campus:token:blacklist:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // ==================== 个人资料 ====================

    @Test
    void testGetProfile() {
        BaseContext.setCurrentId(TEST_USER_ID);

        UserVO vo = userProfileService.getProfile();

        assertNotNull(vo, "用户资料不应为 null");
        assertEquals(TEST_USER_ID, vo.getId(), "用户 ID 应匹配");
        assertNotNull(vo.getUsername(), "用户名不应为 null");
    }

    @Test
    void testUpdateProfile() {
        BaseContext.setCurrentId(TEST_USER_ID);

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickname("新昵称");
        dto.setEmail("newemail@example.com");

        userProfileService.updateProfile(dto);

        UserVO vo = userProfileService.getProfile();
        assertEquals("新昵称", vo.getNickname(), "昵称应已更新");
        assertEquals("newemail@example.com", vo.getEmail(), "邮箱应已更新");
    }

    @Test
    void testUpdatePassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser001");
        loginDTO.setPassword("123456");
        LoginVO loginVO = userService.login(loginDTO);
        String token = loginVO.getToken();

        assertNotNull(token, "登录后 token 不应为 null");

        BaseContext.setCurrentId(loginVO.getUserId());

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("123456");
        dto.setNewPassword("abcdef");

        userProfileService.updatePassword(dto, token);

        String key = String.format(RedisKeyConstant.TOKEN_BLACKLIST, token);
        Boolean exists = redisTemplate.hasKey(key);
        assertEquals(Boolean.TRUE, exists, "旧 token 应加入黑名单");

        redisTemplate.delete(key);
    }

    @Test
    void testUpdatePasswordWrongOld() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser001");
        loginDTO.setPassword("123456");
        LoginVO loginVO = userService.login(loginDTO);

        BaseContext.setCurrentId(loginVO.getUserId());

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrongpassword");
        dto.setNewPassword("abcdef");

        String token = loginVO.getToken();

        assertThrows(BaseException.class,
                () -> userProfileService.updatePassword(dto, token),
                "原密码错误应抛出 BaseException");
    }

    // ==================== 校园认证 ====================

    @Test
    void testSubmitCertification() {
        BaseContext.setCurrentId(TEST_USER_ID);

        CertificationDTO dto = new CertificationDTO();
        dto.setSchool("某某大学");
        dto.setStudentNo("20210001");
        dto.setRealName("张三");
        dto.setCardImage("https://oss.xxx.com/card.jpg");

        Long certId = userProfileService.submitCertification(dto);
        assertNotNull(certId, "认证提交后 certId 不应为 null");

        CertificationVO vo = userProfileService.getCertification();
        assertNotNull(vo, "认证信息不应为 null");
        assertEquals("某某大学", vo.getSchool(), "学校应匹配");
        assertEquals("20210001", vo.getStudentNo(), "学号应匹配");
    }

    @Test
    void testGetCertificationEmpty() {
        BaseContext.setCurrentId(99999L);

        CertificationVO vo = userProfileService.getCertification();
        assertNull(vo, "无认证记录的用户应返回 null");
    }

    // ==================== 地址管理 ====================

    @Test
    void testAddAddress() {
        BaseContext.setCurrentId(TEST_USER_ID);

        AddressDTO dto = new AddressDTO();
        dto.setReceiver("李四");
        dto.setPhone("13900139000");
        dto.setRegion("某某大学 3 栋");
        dto.setDetail("502 室");
        dto.setIsDefault(UserConstant.ADDRESS_DEFAULT);

        Long addressId = userProfileService.addAddress(dto);
        assertNotNull(addressId, "新增地址后 addressId 不应为 null");

        List<AddressVO> list = userProfileService.listAddresses();
        assertFalse(list.isEmpty(), "地址列表不应为空");
        assertTrue(list.stream().anyMatch(a -> addressId.equals(a.getId())),
                "列表中应包含新增的地址");
    }

    @Test
    void testUpdateAddress() {
        BaseContext.setCurrentId(TEST_USER_ID);

        AddressDTO addDto = new AddressDTO();
        addDto.setReceiver("李四");
        addDto.setPhone("13900139000");
        addDto.setRegion("某某大学 3 栋");
        addDto.setDetail("502 室");
        addDto.setIsDefault(UserConstant.ADDRESS_NOT_DEFAULT);
        Long addressId = userProfileService.addAddress(addDto);

        AddressDTO updateDto = new AddressDTO();
        updateDto.setReceiver("王五");
        updateDto.setPhone("13700137000");
        updateDto.setRegion("某某大学 5 栋");
        updateDto.setDetail("303 室");
        updateDto.setIsDefault(UserConstant.ADDRESS_DEFAULT);

        userProfileService.updateAddress(addressId, updateDto);

        List<AddressVO> list = userProfileService.listAddresses();
        AddressVO updated = list.stream()
                .filter(a -> addressId.equals(a.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(updated, "更新后的地址应存在");
        assertEquals("王五", updated.getReceiver(), "收件人应已更新");
        assertEquals("13700137000", updated.getPhone(), "电话应已更新");
        assertEquals("303 室", updated.getDetail(), "详细地址应已更新");
    }

    @Test
    void testDeleteAddress() {
        BaseContext.setCurrentId(TEST_USER_ID);

        AddressDTO dto = new AddressDTO();
        dto.setReceiver("赵六");
        dto.setPhone("13600136000");
        dto.setDetail("测试地址");
        dto.setIsDefault(UserConstant.ADDRESS_NOT_DEFAULT);
        Long addressId = userProfileService.addAddress(dto);

        userProfileService.deleteAddress(addressId);

        List<AddressVO> list = userProfileService.listAddresses();
        assertTrue(list.stream().noneMatch(a -> addressId.equals(a.getId())),
                "删除后列表中不应再有该地址");
    }

    @Test
    void testDeleteAddressNoPermission() {
        BaseContext.setCurrentId(TEST_USER_ID);

        AddressDTO dto = new AddressDTO();
        dto.setReceiver("赵六");
        dto.setPhone("13600136000");
        dto.setDetail("测试地址");
        Long addressId = userProfileService.addAddress(dto);

        BaseContext.setCurrentId(99999L);

        assertThrows(BaseException.class,
                () -> userProfileService.deleteAddress(addressId),
                "删除他人地址应抛出 BaseException");
    }
}