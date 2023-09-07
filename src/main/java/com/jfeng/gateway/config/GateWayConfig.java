package com.jfeng.gateway.config;

import lombok.Data;
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
    private String accessType;//tcp udp mqtt
    private boolean enable = false;
    private int port;
    private String protocol;

    private Up up;
    private Setting down;


    @Data
    class Up {
        List<DispatchSetting> eventDispatch;
        Setting dataDispatch;
    }

    @Data
    class Down {
        private String type;
        private String topic;
    }

    @Data
    class DispatchSetting {
        private String type;
        private Map<String, String> topic;
    }

    class Setting {
        private String type;
        private String topic;
    }
}
