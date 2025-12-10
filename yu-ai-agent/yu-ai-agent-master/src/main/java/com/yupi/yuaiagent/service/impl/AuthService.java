package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.MiningAgentUser;
import com.yupi.yuaiagent.service.IAuthService;
import com.yupi.yuaiagent.service.IMiningAgentUserService;
import com.yupi.yuaiagent.util.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService implements IAuthService {

    private final IMiningAgentUserService userService;

    public AuthService(IMiningAgentUserService userService) {
        this.userService = userService;
    }

    @Override
    public Result<?> register(String account, String password, String userName, String userRole) {
        if (account == null || account.isEmpty() || password == null || password.isEmpty()) {
            return Result.fail("账号和密码不能为空");
        }

        MiningAgentUser existUser = userService.getUserByAccount(account);
        if (existUser != null) {
            return Result.fail("账号已存在");
        }

        MiningAgentUser user = new MiningAgentUser();
        user.setUserId(UUID.randomUUID().toString());
        user.setAccount(account);
        user.setPassword(password);
        user.setUserName(userName);
        user.setUserRole(userRole);
        user.setCreateTime(LocalDateTime.now());
        user.setIsDeleted(false);

        int result = userService.addUser(user);
        if (result > 0) {
            return Result.success("注册成功", user.getUserId());
        }
        return Result.fail("注册失败");
    }

    @Override
    public Result<?> login(String account, String password) {
        if (account == null || account.isEmpty() || password == null || password.isEmpty()) {
            return Result.fail("账号和密码不能为空");
        }

        MiningAgentUser user = userService.getUserByAccount(account);
        if (user == null) {
            return Result.fail("账号或密码错误");
        }

        if (!password.equals(user.getPassword())) {
            return Result.fail("账号或密码错误");
        }

        if (user.getIsDeleted()) {
            return Result.fail("用户已被禁用");
        }

        return Result.success("登录成功", user.getUserId());
    }

    @Override
    public Result<?> logout(String userId) {
        return Result.success("登出成功", null);
    }
}
