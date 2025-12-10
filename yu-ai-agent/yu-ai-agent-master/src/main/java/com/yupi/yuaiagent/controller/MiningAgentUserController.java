package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.domin.entity.MiningAgentUser;
import com.yupi.yuaiagent.service.IMiningAgentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 矿工/技术人员用户管理接口
 */
@RestController
@RequestMapping("/user")
@Transactional
public class MiningAgentUserController {

    private final IMiningAgentUserService miningAgentUserService;

    public MiningAgentUserController(IMiningAgentUserService miningAgentUserService) {
        this.miningAgentUserService = miningAgentUserService;
    }

    /**
     * 新增用户
     *
     * @param userName 用户名/
     * @param userRole 用户角色
     * @return 响应结果
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addUser(
            @RequestParam String userName,
            @RequestParam String userRole) {

        Map<String, Object> response = new HashMap<>();
        try {
            MiningAgentUser user = new MiningAgentUser();
            user.setUserId(UUID.randomUUID().toString());
            user.setUserName(userName);
            user.setUserRole(userRole);
            user.setCreateTime(LocalDateTime.now());
            user.setIsDeleted(false);

            int result = miningAgentUserService.addUser(user);

            if (result > 0) {
                response.put("code", 200);
                response.put("message", "用户添加成功");
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "用户添加失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "添加用户异常: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 响应结果
     */
    @GetMapping("/get/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String userId) {

        Map<String, Object> response = new HashMap<>();
        try {
            MiningAgentUser user = miningAgentUserService.getUserById(userId);

            if (user != null) {
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "查询用户异常: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据角色查询用户列表
     *
     * @param userRole 用户角色
     * @return 响应结果
     */
    @GetMapping("/list-by-role")
    public ResponseEntity<Map<String, Object>> getUsersByRole(@RequestParam String userRole) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<MiningAgentUser> users = miningAgentUserService.getUsersByRole(userRole);

            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", users);
            response.put("total", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "查询用户列表异常: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @param userRole 用户角色
     * @return 响应结果
     */
    @PutMapping("/update/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userRole) {

        Map<String, Object> response = new HashMap<>();
        try {
            MiningAgentUser user = miningAgentUserService.getUserById(userId);

            if (user == null) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 只更新提供的字段
            if (userName != null && !userName.isEmpty()) {
                user.setUserName(userName);
            }
            if (userRole != null && !userRole.isEmpty()) {
                user.setUserRole(userRole);
            }

            int result = miningAgentUserService.updateUser(user);

            if (result > 0) {
                response.put("code", 200);
                response.put("message", "用户更新成功");
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "用户更新失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "更新用户异常: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除用户（软删除）
     *
     * @param userId 用户ID
     * @return 响应结果
     */
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {

        Map<String, Object> response = new HashMap<>();
        try {
            MiningAgentUser user = miningAgentUserService.getUserById(userId);

            if (user == null) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            int result = miningAgentUserService.deleteUser(userId);

            if (result > 0) {
                response.put("code", 200);
                response.put("message", "用户删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "用户删除失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除用户异常: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
