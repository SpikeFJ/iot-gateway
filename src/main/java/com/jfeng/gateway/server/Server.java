package com.jfeng.gateway.server;

import java.util.Map;

public interface Server {

    void init(Map<String,String> parameter);

    void start() throws Exception;

    void stop();
}
