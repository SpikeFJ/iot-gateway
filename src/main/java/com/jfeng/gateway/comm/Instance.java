package com.jfeng.gateway.comm;

import java.util.HashMap;
import java.util.Map;

public class Instance {

    private Map<String, CollectionSetting> settings = new HashMap<>();

    static {
        CollectionSetting collectionSetting = new CollectionSetting();
        collectionSetting.setDtuCode("3296386058158484");
        collectionSetting.setHeartCode("iot.haokuai.cn");
        collectionSetting.setConnectPeriod(10000);

        SlaveSetting slaveSetting = new SlaveSetting();
        slaveSetting.setName("从站4");
        slaveSetting.setAddress(4);
        collectionSetting.getSlaveSettings().add(slaveSetting);

        RegisterSetting registerSetting1 = new RegisterSetting();
        registerSetting1.setCode("temperature");
        registerSetting1.setAddress(0);
        registerSetting1.setLength(1);
        registerSetting1.setUnit("摄氏度");

        RegisterSetting registerSetting2 = new RegisterSetting();
        registerSetting2.setCode("humidity");
        registerSetting2.setAddress(1);
        registerSetting2.setLength(1);
        registerSetting2.setUnit("%");

        slaveSetting.getRegisterSettings().add(registerSetting1);
        slaveSetting.getRegisterSettings().add(registerSetting2);
    }
}
