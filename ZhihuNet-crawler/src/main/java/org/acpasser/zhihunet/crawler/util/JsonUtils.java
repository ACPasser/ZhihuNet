package org.acpasser.zhihunet.crawler.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 反序列化
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // 序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 命名策略
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // 工厂配置
        objectMapper.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), false);
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T fromJson(String json, JavaType type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> type) {
        try {
            if (json != null) {
                return objectMapper.readValue(json, type);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static Map<String, Object> jsonToMap(String json) {
        return fromJson(json, new TypeReference<>() {});
    }
}
