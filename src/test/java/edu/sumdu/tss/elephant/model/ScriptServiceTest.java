package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ScriptServiceTest {
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
        sql2o = Mockito.mock(Sql2o.class);
        dbPool.when(DBPool::getConnection).thenReturn(sql2o);
        dbPool.when(() -> DBPool.dbUtilUrl(anyString())).thenCallRealMethod();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        keys.close();
        dbPool.close();
    }

    @BeforeEach
    void setUp() {
        connection = Mockito.mock(Connection.class);
        when(sql2o.open()).thenReturn(connection);
        query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.addParameter(anyString(), any(Integer.class))).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
    }

    @Test
    void testList() {
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.executeAndFetch(Script.class)).thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(), ScriptService.list("database"));
    }

    @Test
    void testById() {
        Script script = mock(Script.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.executeAndFetchFirst(Script.class)).thenReturn(script);

        assertEquals(script, ScriptService.byId(1));
    }

    @Test
    void testByIdFailed() {
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.executeAndFetchFirst(Script.class)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> ScriptService.byId(1));
    }

    @Test
    void testSave() {
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
        when(query.bind(any())).thenReturn(query);
        ScriptService.save(new Script());
        verify(query).executeUpdate();
    }
    @Test
    void testDestroy() {
        try(MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
            Script script = mock(Script.class);
            when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
            when(query.bind(any())).thenReturn(query);
            when(script.getPath()).thenReturn("/");
            ScriptService.destroy(script);
            verify(query).executeUpdate();
        }
    }
}