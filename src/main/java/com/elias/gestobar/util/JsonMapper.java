package com.elias.gestobar.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

// utils/JsonMapper.java
public class JsonMapper {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    private JsonMapper() {}

    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar a JSON: " + e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar JSON: " + e.getMessage(), e);
        }
    }

    // Para listas: JsonMapper.fromJsonList(json, ProductDto.class)
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        try {
            var type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, clazz);
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar lista JSON: " + e.getMessage(), e);
        }
    }
}