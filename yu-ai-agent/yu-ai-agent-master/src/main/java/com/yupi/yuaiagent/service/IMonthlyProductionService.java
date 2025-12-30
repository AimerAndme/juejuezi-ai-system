package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.MonthlyProduction;
import java.util.List;

public interface IMonthlyProductionService {

    int add(MonthlyProduction entity);

    MonthlyProduction getById(Long recordId);

    List<MonthlyProduction> getAll();

    List<MonthlyProduction> getByBlockId(String blockId);

    int update(MonthlyProduction entity);

    int delete(Long recordId);
}
