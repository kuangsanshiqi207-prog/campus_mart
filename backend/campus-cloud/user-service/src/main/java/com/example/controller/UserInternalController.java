package com.example.controller;

import com.example.api.user.UserFeignClient;
import com.example.entity.CreditLog;
import com.example.entity.User;
import com.example.mapper.stats.CreditLogMapper;
import com.example.mapper.user.UserMapper;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.vo.stats.CreditLogVO;
import com.example.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class UserInternalController implements UserFeignClient {

    private final UserMapper userMapper;

    private final CreditLogMapper creditLogMapper;

    @Override
    public Result<UserVO> getById(Long id) {
        User user = userMapper.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    @Override
    public Result<UserVO> getByUsername(String username) {
        User user = userMapper.getByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    @Override
    public Result<List<User>> batchGet(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(List.of());
        }
        List<User> users = userMapper.listByIds(ids);
        // 去掉密码字段
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @Override
    public Result<Void> updateStatus(Long userId, String status) {
        User user = userMapper.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        userMapper.updateStatus(userId, status);
        return Result.success();
    }

    @Override
    public Result<PageResult> listCreditLogs(Long userId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 100) pageSize = 10;
        int offset = (pageNum - 1) * pageSize;

        Long total = creditLogMapper.countByUserId(userId);
        List<CreditLog> list = creditLogMapper.listByUserId(userId, offset, pageSize);

        List<CreditLogVO> voList = list.stream()
                .map(log -> {
                    CreditLogVO vo = new CreditLogVO();
                    BeanUtils.copyProperties(log, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        return Result.success(new PageResult(total, voList));
    }

    @Override
    public Result<List<Map<String, Object>>> getCreditTrend(Long userId, String startDate) {
        List<Map<String, Object>> rows = creditLogMapper.getCreditTrend(userId, startDate);
        return Result.success(rows);
    }
}