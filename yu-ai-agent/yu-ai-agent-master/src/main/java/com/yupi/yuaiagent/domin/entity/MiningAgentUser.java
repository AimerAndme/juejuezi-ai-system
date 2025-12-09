package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MiningAgentUser {

    /**
     * 用户唯一标识（UUID/员工编号）
     */
    private String userId;

    /**
     * 登录账号
     */
    private String account;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 用户名（真实姓名/昵称）
     */
    private String userName;

    /**
     * 用户角色（如miner/technician/manager）
     */
    private String userRole;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 软删除标识（false-正常，true-删除）
     */
    private Boolean isDeleted;
}
