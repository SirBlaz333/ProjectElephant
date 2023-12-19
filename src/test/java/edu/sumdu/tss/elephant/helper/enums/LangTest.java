package edu.sumdu.tss.elephant.helper.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LangTest {

    @Test
    void byValue_validValue_returnsEnum() {
        assertEquals(Lang.EN, Lang.byValue("en"));
        assertEquals(Lang.UK, Lang.byValue("uk"));
        assertEquals(Lang.EN, Lang.byValue("EN")); // Case-insensitive
        assertEquals(Lang.UK, Lang.byValue("uK")); // Case-insensitive
    }

    @Test
    void byValue_invalidValue_throwsException() {
        assertThrows(RuntimeException.class, () -> Lang.byValue("fr"));
        assertThrows(RuntimeException.class, () -> Lang.byValue("invalid"));
    }
}
