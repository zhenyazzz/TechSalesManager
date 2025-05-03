package org.com.techsalesmanagerserver.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T>  T fromJson(String json, Class<T> clazz ) throws JsonProcessingException {
        return mapper.readValue(json, clazz);
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }
}
