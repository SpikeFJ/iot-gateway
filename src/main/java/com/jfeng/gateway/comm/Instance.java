package com.jfeng.gateway.comm;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Instance {

    public static Map<String, CollectionSetting> settings = new HashMap<>();

    static {
        CollectionSetting collectionSetting = new CollectionSetting();
        collectionSetting.setDtuCode("3296386058158484");
        collectionSetting.setHeartCode("iot.haokuai.cn");
        collectionSetting.setConnectPeriod(10);

        SlaveSetting slaveSetting = new SlaveSetting();
        slaveSetting.setName("从站4");
        slaveSetting.setAddress(4);
        collectionSetting.getSlaveSettings().add(slaveSetting);

        RegisterSetting registerSetting1 = new RegisterSetting();
        registerSetting1.setCode("temperature");
        registerSetting1.setDataType(1);
        registerSetting1.setAddress(0);
        registerSetting1.setLength(1);
        registerSetting1.setDecimalLength(2);
        registerSetting1.setExpression("x/10.0");
        registerSetting1.setUnit("摄氏度");

        RegisterSetting registerSetting2 = new RegisterSetting();
        registerSetting2.setCode("humidity");
        registerSetting2.setDataType(1);
        registerSetting2.setAddress(1);
        registerSetting2.setLength(1);
        registerSetting2.setDecimalLength(2);
        registerSetting2.setUnit("%");
        registerSetting2.setExpression("x/10.0");

        slaveSetting.getRegisterSettings().add(registerSetting1);
        slaveSetting.getRegisterSettings().add(registerSetting2);

        collectionSetting.getModbusList();
        settings.put("3296386058158484", collectionSetting);

    }
}
