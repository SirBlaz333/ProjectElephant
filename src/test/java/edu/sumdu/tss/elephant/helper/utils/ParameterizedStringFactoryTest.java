package edu.sumdu.tss.elephant.helper.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParameterizedStringFactoryTest {

    private ParameterizedStringFactory instance;
    private static final String test = "select :test from :table";

    @BeforeEach
    void setUp() {
        instance = new ParameterizedStringFactory(test);
    }

    @Test
    void addParameter() {
        String actual = instance.addParameter("test", "TEST").addParameter("table", "TABLE").toString();
        assertEquals("select TEST from TABLE", actual);
    }

    @Test
    void addParameterWithNullValue() {
        String actual = instance.addParameter("test", null).toString();
        assertEquals("select null from :table", actual);
    }

    @Test
    void addDuplicateParameter() {
        instance.addParameter("test", "TEST");
        String actual = instance.addParameter("test", "NEW_TEST").toString();
        assertEquals("select NEW_TEST from :table", actual);
    }

    @Test
    void testToString() {
        assertEquals(test, instance.toString());
    }
}
