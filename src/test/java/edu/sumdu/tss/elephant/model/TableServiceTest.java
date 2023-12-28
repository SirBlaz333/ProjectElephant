package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TableServiceTest {
    private static MockedStatic<Keys> keys;
    private static MockedStatic<DBPool> dbPool;
    private static Sql2o sql2o;
    private Connection connection;
    private Query query;
    @BeforeAll
    static void setUpAll() {
        keys = mockStatic(Keys.class);
        keys.when(() -> Keys.get("DB.NAME")).thenReturn("db");
        keys.when(() -> Keys.get("DB.URL")).thenReturn("localhost");
        keys.when(() -> Keys.get("DB.PORT")).thenReturn("5432");
        keys.when(() -> Keys.get("DB.USERNAME")).thenReturn("username");
        keys.when(() -> Keys.get("DB.PASSWORD")).thenReturn("password");
        dbPool = mockStatic(DBPool.class);
        sql2o = mock(Sql2o.class);
        dbPool.when(() -> DBPool.getConnection(anyString())).thenReturn(sql2o);
        dbPool.when(() -> DBPool.dbUtilUrl(anyString())).thenCallRealMethod();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        keys.close();
        dbPool.close();
    }

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        when(sql2o.open()).thenReturn(connection);
        query = mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.addParameter(anyString(), any(Integer.class))).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
    }

    @Test
    void testList() {
        when(connection.createQuery(anyString())).thenReturn(query);
        Table table = mock(Table.class);
        when(query.executeAndFetchTable()).thenReturn(table);

        assertEquals(table, TableService.list("database"));
    }

    @Test
    void testTableSize() {
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.executeScalar(Integer.class)).thenReturn(1);

        assertEquals(1, TableService.getTableSize("database", "table"));
    }

    @Test
    void testByName() {
        when(connection.createQuery(anyString())).thenReturn(query);
        Table table = mock(Table.class);
        when(query.executeAndFetchTable()).thenReturn(table);

        assertEquals(table, TableService.list("database"));
    }
}