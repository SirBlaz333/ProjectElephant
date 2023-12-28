package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.AccessRestrictedException;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DatabaseServiceTest {
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
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
    }

    @Test
    public void testActivate() throws Exception {
        Database database = Mockito.mock(Database.class);
        when(database.getOwner()).thenReturn("owner");
        when(query.executeAndFetchFirst(Database.class)).thenReturn(database);

        assertEquals(database, DatabaseService.activeDatabase("owner", "database"));
    }

    @Test
    public void testActivateFailed() throws Exception {
        Database database = Mockito.mock(Database.class);
        when(database.getOwner()).thenReturn("notOwner");
        when(query.executeAndFetchFirst(Database.class)).thenReturn(database);

        assertThrows(AccessRestrictedException.class, () -> DatabaseService.activeDatabase("owner", "database"));
    }

    @Test
    public void testActivateFailed2() throws Exception {
        when(query.executeAndFetchFirst(Database.class)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> DatabaseService.activeDatabase("owner", "database"));
    }

    @Test
    public void testExists() throws Exception {
        assertFalse(DatabaseService.exists( "database"));
    }

    @Test
    public void testExists2() throws Exception {
        when(query.executeScalar(String.class)).thenReturn("");
        assertTrue(DatabaseService.exists( "database"));
    }

    @Test
    public void testForUser() throws Exception {
        when(query.executeAndFetch(Database.class)).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(),DatabaseService.forUser( "owner"));
    }

    @Test
    public void testUserSize() throws Exception {
        when(query.executeAndFetch(Database.class)).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(),DatabaseService.forUser( "owner"));
    }

    @Test
    public void testCreate() throws Exception {
        when(connection.createQuery(anyString())).thenReturn(query);
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
        when(query.executeAndFetch(Database.class)).thenReturn(Collections.emptyList());

        DatabaseService.create("dbname", "owner", "tablespace");
        verify(query, times(2)).executeUpdate();
    }

    @Test
    public void testDrop() throws Exception {
        try(MockedStatic<BackupService> backupService = mockStatic(BackupService.class);
            MockedStatic<ScriptService> scriptService = mockStatic(ScriptService.class)) {
            backupService.when(() -> BackupService.list(anyString())).thenReturn(Collections.emptyList());
            scriptService.when(() -> ScriptService.list(anyString())).thenReturn(Collections.emptyList());

            when(connection.createQuery(anyString())).thenReturn(query);
            when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
            when(query.executeAndFetch(Database.class)).thenReturn(Collections.emptyList());

            DatabaseService.create("dbname", "owner", "tablespace");
            verify(query, times(2)).executeUpdate();
        }
    }
}