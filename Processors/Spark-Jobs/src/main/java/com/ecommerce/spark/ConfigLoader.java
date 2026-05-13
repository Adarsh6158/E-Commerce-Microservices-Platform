package com.ecommerce.spark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;

public class ConfigLoader {
    private static JsonNode config;

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (input != null) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                config = mapper.readTree(input);
            } else {
                System.err.println("Warning: application.yml not found in classpath.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static JsonNode navigate(String path) {
        if (config == null) return null;
        String[] keys = path.split("\\.");
        JsonNode current = config;
        for (String key : keys) {
            if (current != null && current.has(key)) {
                current = current.get(key);
            } else {
                return null;
            }
        }
        return current;
    }

    public static String get(String key, String defaultValue) {
        JsonNode node = navigate(key);
        return node != null ? node.asText() : defaultValue;
    }
}
