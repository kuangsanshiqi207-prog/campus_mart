package com.example.controller;

import com.example.constant.RedisKeyConstant;
import com.example.constant.UserConstant;
import com.example.dto.user.AddressDTO;
import com.example.dto.user.CertificationDTO;
import com.example.dto.user.LoginDTO;
import com.example.dto.user.PasswordUpdateDTO;
import com.example.dto.user.UserUpdateDTO;
import com.example.service.user.UserService;
import com.example.vo.user.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserProfileControllerTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private String token;

    @BeforeEach
    void setUp() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser001");
        dto.setPassword("123456");
        LoginVO vo = userService.login(dto);
        token = "Bearer " + vo.getToken();

        redisTemplate.delete(String.format(RedisKeyConstant.TOKEN_BLACKLIST, vo.getToken()));
    }

    @AfterEach
    void tearDown() {
        if (token != null) {
            String rawToken = token.replace("Bearer ", "");
            redisTemplate.delete(String.format(RedisKeyConstant.TOKEN_BLACKLIST, rawToken));
        }
    }

    // ==================== 个人资料 ====================

    @Test
    void testGetProfile() throws Exception {
        mockMvc.perform(get("/user/profile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser001"));
    }

    @Test
    void testGetProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateProfile() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickname("新昵称");
        dto.setEmail("new@example.com");

        mockMvc.perform(put("/user/profile")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/user/profile")
                        .header("Authorization", token))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.email").value("new@example.com"));
    }

    @Test
    void testUpdateProfileInvalidEmail() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("not-an-email");

        mockMvc.perform(put("/user/profile")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("邮箱格式错误"));
    }

    // ==================== 修改密码 ====================

    @Test
    void testUpdatePassword() throws Exception {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("123456");
        dto.setNewPassword("abcdef");

        mockMvc.perform(put("/user/profile/password")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testUpdatePasswordWrongOld() throws Exception {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrong");
        dto.setNewPassword("abcdef");

        mockMvc.perform(put("/user/profile/password")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("原密码错误"));
    }

    @Test
    void testUpdatePasswordBlank() throws Exception {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("");
        dto.setNewPassword("");

        mockMvc.perform(put("/user/profile/password")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 校园认证 ====================

    @Test
    void testSubmitCertification() throws Exception {
        CertificationDTO dto = new CertificationDTO();
        dto.setSchool("某某大学");
        dto.setStudentNo("20210001");
        dto.setRealName("张三");
        dto.setCardImage("https://oss.xxx.com/card.jpg");

        mockMvc.perform(post("/user/certification")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void testSubmitCertificationInvalid() throws Exception {
        CertificationDTO dto = new CertificationDTO();
        dto.setSchool("");
        dto.setStudentNo("");
        dto.setRealName("");

        mockMvc.perform(post("/user/certification")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void testGetCertification() throws Exception {
        CertificationDTO dto = new CertificationDTO();
        dto.setSchool("某某大学");
        dto.setStudentNo("20210001");
        dto.setRealName("张三");
        dto.setCardImage("https://oss.xxx.com/card.jpg");

        mockMvc.perform(post("/user/certification")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/user/certification")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.school").value("某某大学"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    // ==================== 地址管理 ====================

    @Test
    void testAddAddress() throws Exception {
        AddressDTO dto = new AddressDTO();
        dto.setReceiver("李四");
        dto.setPhone("13900139000");
        dto.setRegion("某某大学 3 栋");
        dto.setDetail("502 室");
        dto.setIsDefault(UserConstant.ADDRESS_NOT_DEFAULT);

        mockMvc.perform(post("/user/addresses")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void testAddAddressInvalidPhone() throws Exception {
        AddressDTO dto = new AddressDTO();
        dto.setReceiver("李四");
        dto.setPhone("123");
        dto.setDetail("502 室");

        mockMvc.perform(post("/user/addresses")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("手机号格式错误"));
    }

    @Test
    void testListAddresses() throws Exception {
        AddressDTO dto = new AddressDTO();
        dto.setReceiver("李四");
        dto.setPhone("13900139000");
        dto.setDetail("502 室");

        mockMvc.perform(post("/user/addresses")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/user/addresses")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testUpdateAddress() throws Exception {
        AddressDTO addDto = new AddressDTO();
        addDto.setReceiver("李四");
        addDto.setPhone("13900139000");
        addDto.setDetail("502 室");

        String response = mockMvc.perform(post("/user/addresses")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andReturn().getResponse().getContentAsString();

        Long addressId = objectMapper.readTree(response).get("data").asLong();

        AddressDTO updateDto = new AddressDTO();
        updateDto.setReceiver("王五");
        updateDto.setPhone("13700137000");
        updateDto.setDetail("303 室");
        updateDto.setIsDefault(UserConstant.ADDRESS_DEFAULT);

        mockMvc.perform(put("/user/addresses/" + addressId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testDeleteAddress() throws Exception {
        AddressDTO dto = new AddressDTO();
        dto.setReceiver("李四");
        dto.setPhone("13900139000");
        dto.setDetail("502 室");

        String response = mockMvc.perform(post("/user/addresses")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long addressId = objectMapper.readTree(response).get("data").asLong();

        mockMvc.perform(delete("/user/addresses/" + addressId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testDeleteAddressNotFound() throws Exception {
        mockMvc.perform(delete("/user/addresses/999999")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("地址不存在"));
    }
}