package com.jfeng.gateway.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageInfo<T> {
    int pageSize;
    int pageNum;
    int totalPages;
    int totalRecords;

    T data;

    public static <T> PageInfo<T> create(int totalRecords, T data, int pageSize,int pageNum) {
        PageInfo<T> pageInfo = new PageInfo<>();
        pageInfo.data = data;
        pageInfo.totalRecords = totalRecords;
        pageInfo.totalPages = totalRecords % pageSize == 0
                ? totalRecords / pageSize
                : totalRecords / pageSize + 1;
        pageInfo.pageNum = pageNum;
        pageInfo.pageSize = pageSize;
        return pageInfo;
    }

    public static int skip(int totalRecords, int pageNum, int pageSize) {
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
