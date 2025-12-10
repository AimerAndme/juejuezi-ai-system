package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.MiningAgentUser;
import com.yupi.yuaiagent.mapper.MiningAgentUserMapper;
import com.yupi.yuaiagent.service.IMiningAgentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 矿工/技术人员用户服务实现
 */
@Service
@Transactional
public class MiningAgentUserService implements IMiningAgentUserService {

    private final MiningAgentUserMapper miningAgentUserMapper;

    public MiningAgentUserService(MiningAgentUserMapper miningAgentUserMapper) {
        this.miningAgentUserMapper = miningAgentUserMapper;
    }

    @Override
    public int addUser(MiningAgentUser user) {
        return miningAgentUserMapper.insert(user);
    }

    @Override
    public MiningAgentUser getUserById(String userId) {
        return miningAgentUserMapper.selectById(userId);
    }

    @Override
    public MiningAgentUser getUserByAccount(String account) {
        return miningAgentUserMapper.selectByAccount(account);
    }

    @Override
    public List<MiningAgentUser> getUsersByRole(String userRole) {
        return miningAgentUserMapper.selectByRole(userRole);
    }

    @Override
    public int updateUser(MiningAgentUser user) {
        return miningAgentUserMapper.update(user);
    }

    @Override
    public int deleteUser(String userId) {
        return miningAgentUserMapper.softDelete(userId);
    }
}
