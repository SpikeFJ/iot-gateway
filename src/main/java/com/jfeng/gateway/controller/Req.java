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
}
