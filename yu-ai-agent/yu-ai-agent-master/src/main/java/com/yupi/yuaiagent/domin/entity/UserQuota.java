package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class UserQuota {

    private String userId;

    private int quotaQpm;

    private int quotaTpm;

    private int quotaConcurrent;

    private int userLevel;

    private int status;

    private int quotaPercent;

    private int quotaWindows;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setQuotaQpm(int quotaQpm) {
        this.quotaQpm = quotaQpm;
    }

    public void setQuotaTpm(int quotaTpm) {
        this.quotaTpm = quotaTpm;
    }

    public void setQuotaConcurrent(int quotaConcurrent) {
        this.quotaConcurrent = quotaConcurrent;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setQuotaPercent(int quotaPercent) {
        this.quotaPercent = quotaPercent;
    }

    public void setQuotaWindows(int quotaWindows) {
        this.quotaWindows = quotaWindows;
    }
}
