package edu.sumdu.tss.elephant.helper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PairTest {

    @Test
    void getKey_shouldReturnKey() {
        Integer key = 42;
        String value = "Hello";
        Pair<Integer, String> pair = new Pair<>(key, value);

        Integer result = pair.getKey();

        assertEquals(key, result);
    }

    @Test
    void getValue_shouldReturnValue() {
        Integer key = 42;
        String value = "Hello";
        Pair<Integer, String> pair = new Pair<>(key, value);

        String result = pair.getValue();

        assertEquals(value, result);
    }

    @Test
    void constructor_shouldSetKeyAndValue() {
        Integer key = 42;
        String value = "Hello";

        Pair<Integer, String> pair = new Pair<>(key, value);

        assertEquals(key, pair.getKey());
        assertEquals(value, pair.getValue());
    }

    @Test
    void defaultConstructor_shouldInitializeKeyAndValueToNull() {
        Pair<Integer, String> pair = new Pair<>();

        assertNull(pair.getKey());
        assertNull(pair.getValue());
    }
}