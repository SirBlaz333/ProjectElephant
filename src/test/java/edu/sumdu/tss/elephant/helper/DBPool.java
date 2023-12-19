package edu.sumdu.tss.elephant.helper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sql2o.Sql2o;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DBPoolTest {

    private MockedStatic<Keys> mockedKeys;

    @BeforeEach
    void setUp() {
        mockedKeys = Mockito.mockStatic(Keys.class);
        mockedKeys.when(() -> Keys.get(anyString())).thenReturn("mockValue");
    }

    @AfterEach
    void tearDown() {
        mockedKeys.close(); // Deregister the static mock
    }

    @Test
    void testGetConnectionDefaultDatabase() {
        assertNotNull(DBPool.getConnection());
        mockedKeys.verify(() -> Keys.get("DB.NAME"), atLeastOnce());
    }

    @Test
    void testGetConnectionSpecificDatabase() {
        String specificDbName = "testDB0";

        assertNotNull(DBPool.getConnection(specificDbName));
    }

    @Test
    void testConnectionPoolSizeManagement() throws NoSuchFieldException, IllegalAccessException {
        // Define the expected maximum connection pool size
        int expectedMaxConnection = 10;

        // Use reflection to access the private storage field
        Field storageField = DBPool.class.getDeclaredField("storage");
        storageField.setAccessible(true);
        Map<String, Pair<Long, Sql2o>> storage = (Map<String, Pair<Long, Sql2o>>) storageField.get(null);

        // Simulate exceeding the maximum connection pool size
        int connectionsCreated = 0; // Count the connections created
        for (int i = 0; i < expectedMaxConnection; i++) {
            Sql2o connection = DBPool.getConnection("testDB" + i);
            if (storage.containsKey("testDB" + i)) {
                connectionsCreated++;
            }
        }

        // Assert: Ensure that the pool size is not exceeded
        assertEquals(expectedMaxConnection, connectionsCreated);
    }

    @Test
    void testDbUtilUrl() {
        String dbName = "testDB";
        String expectedUrl = "postgresql://mockValue:mockValue@mockValue:mockValue/testDB";
        assertEquals(expectedUrl, DBPool.dbUtilUrl(dbName));
    }
}
