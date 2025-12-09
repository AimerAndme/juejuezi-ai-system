package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.MiningAgentUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MiningAgentUserMapper {

    /**
     * 新增用户
     */
    int insert(MiningAgentUser user);

    /**
     * 根据ID查询用户（未删除）
     */
    MiningAgentUser selectById(@Param("userId") String userId);

    /**
     * 根据账号查询用户（未删除）
     */
    MiningAgentUser selectByAccount(@Param("account") String account);

    /**
     * 根据角色查询用户（未删除）
     */
    List<MiningAgentUser> selectByRole(@Param("userRole") String userRole);

    /**
     * 软删除用户
     */
    int softDelete(@Param("userId") String userId);

    /**
     * 更新用户信息
     */
    int update(MiningAgentUser user);
}
