package com.jfeng.gateway.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Req {
    private String startTime;
    private String endTime;
    private String deviceId;
    private String businessId;
    private int pageSize;
    private int pageNum;

    public int skip(int totalRecords) {
        if (pageNum <= 1) {
            return 0;
        }
        if ((pageNum - 1) * pageSize > totalRecords) {
            return totalRecords / pageSize;
        }
        return (pageNum - 1) * pageSize;
    }
}
