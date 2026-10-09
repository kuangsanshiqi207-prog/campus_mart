package com.example.api.user;

import com.example.entity.User;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.vo.user.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", path = "/internal/user")
public interface UserFeignClient {

    /** 按 ID 查用户（不含密码） */
    @GetMapping("/getById")
    Result<UserVO> getById(@RequestParam("id") Long id);

    /** 按用户名查用户（不含密码） */
    @GetMapping("/getByUsername")
    Result<UserVO> getByUsername(@RequestParam("username") String username);

    /** 批量查用户（供 message-service 查昵称头像） */
    @PostMapping("/batchGet")
    Result<List<User>> batchGet(@RequestBody List<Long> ids);

    /** 修改用户状态（封禁/解封） */
    @PutMapping("/updateStatus")
    Result<Void> updateStatus(@RequestParam("userId") Long userId,
                              @RequestParam("status") String status);

    /** 信用分流水（供 biz 的 StatsServiceImpl 调用） */
    @GetMapping("/creditLogs")
    Result<PageResult> listCreditLogs(@RequestParam("userId") Long userId,
                                      @RequestParam("pageNum") Integer pageNum,
                                      @RequestParam("pageSize") Integer pageSize);

    /** 信用分趋势（供 biz 的 StatsServiceImpl 调用） */
    @GetMapping("/creditTrend")
    Result<List<Map<String, Object>>> getCreditTrend(@RequestParam("userId") Long userId,
                                                     @RequestParam("startDate") String startDate);
}