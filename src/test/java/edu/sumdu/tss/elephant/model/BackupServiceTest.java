package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.BackupException;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BackupServiceTest {
    private static MockedStatic<UserService> userService;
    private static MockedStatic<FileUtils> fileUtils;
    private static MockedStatic<CmdUtil> cmdUtil;
    private static MockedStatic<Keys> keys;
    private static MockedStatic<DBPool> dbPool;
    private static Sql2o sql2o;
    private Connection connection;

    @BeforeAll
    static void setUpAll() {
        userService = mockStatic(UserService.class);
        fileUtils = mockStatic(FileUtils.class);
        cmdUtil = mockStatic(CmdUtil.class);
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
        userService.when(() -> UserService.userStoragePath(anyString())).thenReturn("");
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        Files.deleteIfExists(Path.of(BackupService.filePath("owner", "database", "point")));
        userService.close();
        fileUtils.close();
        cmdUtil.close();
        keys.close();
        dbPool.close();
    }

    @BeforeEach
    void setUp() {
        connection = Mockito.mock(Connection.class);
        when(sql2o.open()).thenReturn(connection);
    }

    @Test
    public void testList() throws Exception {
        Query query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        when(query.executeAndFetch(Backup.class)).thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(), BackupService.list("database"));
    }

    @Test
    public void testByName() throws Exception {
        Query query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        Backup backup = Mockito.mock(Backup.class);
        when(query.executeAndFetchFirst(Backup.class)).thenReturn(backup);

        assertEquals(backup, BackupService.byName("database", "point"));
    }

    @Test
    public void testByNameFailed() throws Exception {
        Query query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        when(query.executeAndFetchFirst(Backup.class)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> BackupService.byName("database", "point"));
    }

    @Test
    public void performTest() throws Exception {
        Query query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        when(query.bind(any())).thenReturn(query);
        Backup backup = new Backup();
        when(query.executeAndFetchFirst(Backup.class)).thenReturn(backup);

        BackupService.perform("owner", "database", "point");
        assertEquals(Backup.BackupState.DONE.name(), backup.getStatus());
        assertEquals(new Date().getSeconds(), backup.getUpdatedAt().getSeconds());
    }

    @Test
    public void performNewBackup() throws Exception {
        Query query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        when(query.bind(any())).thenReturn(query);
        when(query.executeAndFetchFirst(Backup.class)).thenReturn(null);

        BackupService.perform("owner", "database", "point");}

    @Test
    public void performTestFailed() throws Exception {
        Query query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        when(query.bind(any())).thenReturn(query);
        when(query.executeAndFetchFirst(Backup.class)).thenThrow(Sql2oException.class);

        assertThrows(BackupException.class, () -> BackupService.perform("owner", "database", "point"));
    }

    @Test
    public void restoreTest() throws Exception {
        Query query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        when(query.bind(any())).thenReturn(query);
        Backup backup = new Backup();
        when(query.executeAndFetchFirst(Backup.class)).thenReturn(backup);

        BackupService.restore("owner", "database", "point");
    }

    @Test
    public void deleteBackup() throws Exception {
        Query query = Mockito.mock(Query.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        when(query.bind(any())).thenReturn(query);
        Backup backup = new Backup();
        when(query.executeAndFetchFirst(Backup.class)).thenReturn(backup);

        assertThrows(RuntimeException.class, () -> BackupService.delete("owner", "database", "point"));
        verify(query).executeUpdate();
    }
}