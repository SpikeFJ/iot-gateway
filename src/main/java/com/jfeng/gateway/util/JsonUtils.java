package com.jfeng.gateway.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * copy from com.gimis.util
 */
@Slf4j
public class JsonUtils {
    private JsonUtils() {

    }
    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        //对于空对象转Json时不报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //允许属性名称没有引号
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //允许单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //设置输入时忽略Json字符串存在但是JSON对象实际没有的属性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
    /**
     * 将对象序列化为JSON字符串
     *
     * @param object
     * @return JSON字符串
     */
    public static String serialize(Object object) {
        Writer write = new StringWriter();
        try {
            objectMapper.writeValue(write, object);
        } catch (JsonGenerationException e) {
            log.error("JsonGenerationException when serialize object to json", e);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException when serialize object to json", e);
        } catch (IOException e) {
            log.error("IOException when serialize object to json", e);
        }
        return write.toString();
    }

    /**
     * 将JSON字符串反序列化为对象
     *
     * @param json
     * @return JSON字符串
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String json, Class<T> clazz) {
        Object object = null;
        try {
            object = objectMapper.readValue(json, TypeFactory.rawClass(clazz));
        } catch (JsonParseException e) {
            log.error("JsonParseException when serialize object to json", e);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException when serialize object to json", e);
        } catch (IOException e) {
            log.error("IOException when serialize object to json", e);
        }
        return (T) object;
    }

    /**
     * 将JSON字符串反序列化为对象
     *
     * @param json
     * @return JSON字符串
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String json, TypeReference<T> typeRef) {
        try {
            return (T) objectMapper.readValue(json, typeRef);
        } catch (JsonParseException e) {
            log.error("JsonParseException when deserialize json", e);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException when deserialize json", e);
        } catch (IOException e) {
            log.error("IOException when deserialize json", e);
        }
        return null;
    }
}
