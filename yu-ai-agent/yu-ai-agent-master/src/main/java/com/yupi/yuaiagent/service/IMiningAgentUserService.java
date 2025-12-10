package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.MiningAgentUser;
import java.util.List;

/**
 * 矿工/技术人员用户服务接口
 */
public interface IMiningAgentUserService {

    /**
     * 新增用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int addUser(MiningAgentUser user);

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    MiningAgentUser getUserById(String userId);

    /**
     * 根据账号查询用户
     *
     * @param account 用户账号
     * @return 用户信息
     */
    MiningAgentUser getUserByAccount(String account);

    /**
     * 根据角色查询用户列表
     *
     * @param userRole 用户角色
     * @return 用户列表
     */
    List<MiningAgentUser> getUsersByRole(String userRole);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int updateUser(MiningAgentUser user);

    /**
     * 删除用户（软删除）
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteUser(String userId);
}
