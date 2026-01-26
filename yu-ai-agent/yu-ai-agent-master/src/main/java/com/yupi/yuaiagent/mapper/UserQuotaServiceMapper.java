package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.UserQuota;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserQuotaServiceMapper {

    int insert(UserQuota userQuota);

    UserQuota selectById(@Param("userId") String userId);

    UserQuota selectByUserLevel(@Param("userLevel") int userLevel);

    UserQuota selectByUserId(@Param("userId") String userId);

    List<UserQuota> selectAll();

    int update(UserQuota userQuota);

    int updateStatus(@Param("userId") String userId, @Param("status") int status);

    int delete(@Param("userId") String userId);
}
