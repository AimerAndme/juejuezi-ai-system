package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.service.IAuthService;
import com.yupi.yuaiagent.service.IMiningAgentUserService;
import com.yupi.yuaiagent.util.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;
    private final IMiningAgentUserService userService;

    public AuthController(IAuthService authService, IMiningAgentUserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * 用户注册接口
     *
     * @param account 账号
     * @param password 密码
     * @param userName 用户名
     * @param userRole 用户角色
     * @return Result，返回用户ID
     */
    @PostMapping("/register")
    public Result<?> register(@RequestParam String account,
            @RequestParam String password,
            @RequestParam String userName,
            @RequestParam String userRole) {
        return authService.register(account, password, userName, userRole);
    }

    /**
     * 用户登录接口
     *
     * @param account 账号
     * @param password 密码
     * @return Result，返回用户ID
     */
    @PostMapping("/login")
    public Result<?> login(@RequestParam String account,
            @RequestParam String password) {
        return authService.login(account, password);
    }

    /**
     * 用户登出接口
     *
     * @param userId 用户ID（从请求头获取）
     * @return Result，返回登出成功消息
     */
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return authService.logout(userId);
    }

    /**
     * 获取当前用户信息接口（测试用）
     *
     * @param userId 用户ID（从请求头获取）
     * @return Result，返回用户信息
     */
    @GetMapping("/test")
    public Result<?> getCurrentUser(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isEmpty()) {
            return Result.fail("用户未登录");
        }

        try {
            var user = userService.getUserById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            return Result.success("获取成功", user);
        } catch (Exception e) {
            return Result.fail("获取用户信息失败");
        }
    }
}
