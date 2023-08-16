package com.jfeng.gateway.config;

import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.session.TcpSessionManage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "gateway")
@Getter
@Setter
public class GateWayConfig {
    public ConfigItem tcp;
    public ConfigItem udp;
    public ConfigItem http;
    public ConfigItem mqtt;
    public ConfigItem websocket;

    @Getter
    @Setter
    public static class ConfigItem {
        private boolean enable = false;
        private int port;
        private List<String> blackIpList;
        private List<String> whiteIpList;
        private Map<String, String> parameter;
    }

    @Bean
    @ConditionalOnProperty(prefix = "gateway", name = "tcp.enable", havingValue = "true")
    public TcpSessionManage<TcpSession> tcpSessionManage() {
        ConfigItem tcpItem = getTcp();

        return null;
    }

}
