package com.jfeng.gateway.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Req {
    private String startTime;
    private String endTime;
    private String query;
    private int pageSize = 10;
    private int pageNum = 1;

    public int skip(int totalRecords) {
        if (pageNum <= 1) {
            return 0;
        }
        int totalPages = totalRecords % pageSize == 0
                ? totalRecords / pageSize
                : totalRecords / pageSize + 1;

        if (pageNum > totalPages) {
            pageNum = totalPages;
        }

        return (pageNum - 1) * pageSize;
    }
}
