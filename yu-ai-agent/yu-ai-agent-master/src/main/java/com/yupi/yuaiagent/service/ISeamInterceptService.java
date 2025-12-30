package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.SeamIntercept;
import java.util.List;

public interface ISeamInterceptService {

    int add(SeamIntercept entity);

    SeamIntercept getById(Long interceptId);

    List<SeamIntercept> getAll();

    List<SeamIntercept> getByHoleId(String holeId);

    int update(SeamIntercept entity);

    int delete(Long interceptId);
}
