package edu.sumdu.tss.elephant.helper.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void randomAlphaString() {
        int targetStringLength = 10;
        String randomString = StringUtils.randomAlphaString(targetStringLength);
        assertEquals(targetStringLength, randomString.length()); // Check if the length matches the target length
        assertTrue(randomString.matches("[a-z]+")); // Check if the string contains only lowercase alphabets
    }

    @Test
    void uuid() {
        String uuid = StringUtils.uuid();
        assertNotNull(uuid); // Check if UUID is not null
        assertTrue(uuid.matches("[a-f0-9\\-]+")); // Check if the UUID format is valid
    }

    @Test
    void replaceLast() {
        String original = "failed unit test failed";
        String replaced = StringUtils.replaceLast(original, "failed", "passed");
        assertEquals("failed unit test passed", replaced); // Check if the last occurrence is replaced

        // Test with a string that doesn't contain the substring to be replaced
        String noMatch = StringUtils.replaceLast(original, "false", "true");
        assertEquals(original, noMatch); // The string should remain unchanged
    }
}