package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.CoalSeam;
import java.util.List;

public interface ICoalSeamService {

    int add(CoalSeam entity);

    CoalSeam getById(String seamId);

    List<CoalSeam> getAll();

    int update(CoalSeam entity);

    int delete(String seamId);
}
