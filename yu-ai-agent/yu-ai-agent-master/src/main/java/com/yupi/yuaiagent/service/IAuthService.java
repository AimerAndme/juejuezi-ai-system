package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.util.Result;

public interface IAuthService {
    
    Result<?> register(String account, String password, String userName, String userRole);
    
    Result<?> login(String account, String password);
    
    Result<?> logout(String userId);
}
