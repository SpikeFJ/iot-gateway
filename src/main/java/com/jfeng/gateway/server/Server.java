package com.jfeng.gateway.server;

import java.util.Map;

public interface Server {

    void start(Map<String,String> parameter) throws Exception;

    void stop();
}
