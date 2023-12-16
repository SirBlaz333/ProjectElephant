package edu.sumdu.tss.elephant.helper;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {
    
    @Test
    void byValue() {
        assertEquals(UserRole.ANYONE, UserRole.byValue(0));
        assertEquals(UserRole.UNCHEKED, UserRole.byValue(1));
        assertEquals(UserRole.BASIC_USER, UserRole.byValue(2));
        assertEquals(UserRole.PROMOTED_USER, UserRole.byValue(3));
        assertEquals(UserRole.ADMIN, UserRole.byValue(4));

        assertThrows(RuntimeException.class, () -> UserRole.byValue(5));
    }

    @Test
    void maxConnections() {
        assertEquals(0, UserRole.ANYONE.maxConnections());
        assertEquals(0, UserRole.UNCHEKED.maxConnections());
        assertEquals(5, UserRole.BASIC_USER.maxConnections());
        assertEquals(5, UserRole.PROMOTED_USER.maxConnections());
        assertEquals(5, UserRole.ADMIN.maxConnections());
    }

    @Test
    void maxDB() {
        assertEquals(0, UserRole.ANYONE.maxDB());
        assertEquals(0, UserRole.UNCHEKED.maxDB());
        assertEquals(2, UserRole.BASIC_USER.maxDB());
        assertEquals(3, UserRole.PROMOTED_USER.maxDB());
        assertEquals(100, UserRole.ADMIN.maxDB());
    }

    @Test
    void maxStorage() {
        assertEquals(0, UserRole.ANYONE.maxStorage());
        assertEquals(0, UserRole.UNCHEKED.maxStorage());
        assertEquals(20 * FileUtils.ONE_MB, UserRole.BASIC_USER.maxStorage());
        assertEquals(50 * FileUtils.ONE_MB, UserRole.PROMOTED_USER.maxStorage());
        assertEquals(50 * FileUtils.ONE_MB, UserRole.ADMIN.maxStorage());
    }

    @Test
    void maxBackupsPerDB() {
        assertEquals(0, UserRole.ANYONE.maxBackupsPerDB());
        assertEquals(0, UserRole.UNCHEKED.maxBackupsPerDB());
        assertEquals(1, UserRole.BASIC_USER.maxBackupsPerDB());
        assertEquals(5, UserRole.PROMOTED_USER.maxBackupsPerDB());
        assertEquals(10, UserRole.ADMIN.maxBackupsPerDB());
    }

    @Test
    void maxScriptsPerDB() {
        assertEquals(0, UserRole.ANYONE.maxScriptsPerDB());
        assertEquals(0, UserRole.UNCHEKED.maxScriptsPerDB());
        assertEquals(2, UserRole.BASIC_USER.maxScriptsPerDB());
        assertEquals(5, UserRole.PROMOTED_USER.maxScriptsPerDB());
        assertEquals(10, UserRole.ADMIN.maxScriptsPerDB());
    }

    @Test
    void getValue() {
        assertEquals(0L, UserRole.ANYONE.getValue());
        assertEquals(1L, UserRole.UNCHEKED.getValue());
        assertEquals(2L, UserRole.BASIC_USER.getValue());
        assertEquals(3L, UserRole.PROMOTED_USER.getValue());
        assertEquals(4L, UserRole.ADMIN.getValue());
    }

    @Test
    void values() {
        UserRole[] expectedValues = {UserRole.ANYONE, UserRole.UNCHEKED, UserRole.BASIC_USER, UserRole.PROMOTED_USER, UserRole.ADMIN};
        assertArrayEquals(expectedValues, UserRole.values());
    }

    @Test
    void testValueOf() {
        assertEquals(UserRole.ANYONE, UserRole.valueOf("ANYONE"));
        assertEquals(UserRole.UNCHEKED, UserRole.valueOf("UNCHEKED"));
        assertEquals(UserRole.BASIC_USER, UserRole.valueOf("BASIC_USER"));
        assertEquals(UserRole.PROMOTED_USER, UserRole.valueOf("PROMOTED_USER"));
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
    }

    @Test
    void name() {
        assertEquals("ANYONE", UserRole.ANYONE.name());
        assertEquals("UNCHEKED", UserRole.UNCHEKED.name());
        assertEquals("BASIC_USER", UserRole.BASIC_USER.name());
        assertEquals("PROMOTED_USER", UserRole.PROMOTED_USER.name());
        assertEquals("ADMIN", UserRole.ADMIN.name());
    }

    @Test
    void ordinal() {
        assertEquals(0, UserRole.ANYONE.ordinal());
        assertEquals(1, UserRole.UNCHEKED.ordinal());
        assertEquals(2, UserRole.BASIC_USER.ordinal());
        assertEquals(3, UserRole.PROMOTED_USER.ordinal());
        assertEquals(4, UserRole.ADMIN.ordinal());
    }

    @Test
    void testToString() {
        assertEquals("ANYONE", UserRole.ANYONE.toString());
        assertEquals("UNCHEKED", UserRole.UNCHEKED.toString());
        assertEquals("BASIC_USER", UserRole.BASIC_USER.toString());
        assertEquals("PROMOTED_USER", UserRole.PROMOTED_USER.toString());
        assertEquals("ADMIN", UserRole.ADMIN.toString());
    }

    @Test
    void testEquals() {
        assertEquals(UserRole.ANYONE, UserRole.ANYONE);
        assertEquals(UserRole.UNCHEKED, UserRole.UNCHEKED);
        assertEquals(UserRole.BASIC_USER, UserRole.BASIC_USER);
        assertEquals(UserRole.PROMOTED_USER, UserRole.PROMOTED_USER);
        assertEquals(UserRole.ADMIN, UserRole.ADMIN);

        assertNotEquals(UserRole.ANYONE, UserRole.UNCHEKED);
    }

    @Test
    void testHashCode() {
        assertEquals(UserRole.ANYONE.hashCode(), UserRole.ANYONE.hashCode());
        assertEquals(UserRole.UNCHEKED.hashCode(), UserRole.UNCHEKED.hashCode());
        assertEquals(UserRole.BASIC_USER.hashCode(), UserRole.BASIC_USER.hashCode());
        assertEquals(UserRole.PROMOTED_USER.hashCode(), UserRole.PROMOTED_USER.hashCode());
        assertEquals(UserRole.ADMIN.hashCode(), UserRole.ADMIN.hashCode());

        assertNotEquals(UserRole.ANYONE.hashCode(), UserRole.UNCHEKED.hashCode());
    }

    @Test
    void compareTo() {
        assertEquals(0, UserRole.ANYONE.compareTo(UserRole.ANYONE));
        assertEquals(-1, UserRole.ANYONE.compareTo(UserRole.UNCHEKED));
        assertEquals(1, UserRole.UNCHEKED.compareTo(UserRole.ANYONE));
        assertEquals(0, UserRole.BASIC_USER.compareTo(UserRole.BASIC_USER));
        assertEquals(0, UserRole.PROMOTED_USER.compareTo(UserRole.PROMOTED_USER));
        assertEquals(0, UserRole.ADMIN.compareTo(UserRole.ADMIN));
    }

    @Test
    void getDeclaringClass() {
        assertEquals(UserRole.class, UserRole.ANYONE.getDeclaringClass());
        assertEquals(UserRole.class, UserRole.UNCHEKED.getDeclaringClass());
        assertEquals(UserRole.class, UserRole.BASIC_USER.getDeclaringClass());
        assertEquals(UserRole.class, UserRole.PROMOTED_USER.getDeclaringClass());
        assertEquals(UserRole.class, UserRole.ADMIN.getDeclaringClass());
    }

    @Test
    void valueOf() {
        assertEquals(UserRole.ANYONE, UserRole.valueOf(UserRole.ANYONE.name()));
        assertEquals(UserRole.UNCHEKED, UserRole.valueOf(UserRole.UNCHEKED.name()));
        assertEquals(UserRole.BASIC_USER, UserRole.valueOf(UserRole.BASIC_USER.name()));
        assertEquals(UserRole.PROMOTED_USER, UserRole.valueOf(UserRole.PROMOTED_USER.name()));
        assertEquals(UserRole.ADMIN, UserRole.valueOf(UserRole.ADMIN.name()));
    }
}
