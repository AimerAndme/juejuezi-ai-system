package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.LithologyLog;
import java.util.List;

public interface ILithologyLogService {

    int add(LithologyLog entity);

    LithologyLog getById(Long logId);

    List<LithologyLog> getAll();

    List<LithologyLog> getByHoleId(String holeId);

    int update(LithologyLog entity);

    int delete(Long logId);
}
