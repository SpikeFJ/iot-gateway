package com.jfeng.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "gateway")
@Getter
@Setter
public class GateWayConfig {
    private boolean enable = false;
    private int port;
    private List<String> blackIpList;
    private List<String> whiteIpList;
    private Map<String, String> parameter;
}
