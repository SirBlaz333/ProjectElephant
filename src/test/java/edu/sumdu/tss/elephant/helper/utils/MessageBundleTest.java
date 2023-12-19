package edu.sumdu.tss.elephant.helper.utils;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MessageBundleTest {

    @Test
    void testGet() {
        try (
                InputStream enInputStream = MessageBundleTest.class.getResourceAsStream("/i18n/messages_en.yml");
                InputStream ukInputStream = MessageBundleTest.class.getResourceAsStream("/i18n/messages_uk.yml")
        ) {
            Map<String, String> enKeysAndValues = get(enInputStream);
            Map<String, String> ukKeysAndValues = get(ukInputStream);

            MessageBundle enBundle = new MessageBundle("en");
            MessageBundle ukBundle = new MessageBundle("uk");

            assertAll("Check all keys and values",
                    () -> checkKeysAndValues(enBundle, enKeysAndValues),
                    () -> checkKeysAndValues(ukBundle, ukKeysAndValues)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkKeysAndValues(MessageBundle bundle, Map<String, String> keysAndValues) {
        for (Map.Entry<String, String> entry : keysAndValues.entrySet()) {
            String key = entry.getKey();
            String expectedValue = entry.getValue();
            String actualValue = bundle.get(key);

            assertEquals(expectedValue, actualValue, "Key: " + key);
        }
    }

    private Map<String, String> get(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);
        return get(map, "");
    }

    private Map<String, String> get(Map<String, Object> map, String currentPath) {
        Map<String, String> keysAndValues = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String newPath = currentPath.isEmpty() ? key : currentPath + "." + key;

            Object value = entry.getValue();
            if (value instanceof Map) {
                keysAndValues.putAll(get((Map<String, Object>) value, newPath));
            } else {
                // Додаємо ключ і його значення
                keysAndValues.put(newPath, value.toString());
            }
        }
        return keysAndValues;
    }
}
