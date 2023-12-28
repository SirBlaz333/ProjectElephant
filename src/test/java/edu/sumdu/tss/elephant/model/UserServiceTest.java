package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class UserServiceTest {
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
        keys.when(() -> Keys.get("DB.LOCAL_PATH")).thenReturn("path//");
        dbPool = mockStatic(DBPool.class);
        sql2o = mock(Sql2o.class);
        dbPool.when(DBPool::getConnection).thenReturn(sql2o);
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
    void testByLogin() {
        when(connection.createQuery(anyString())).thenReturn(query);
        User user = mock(User.class);
        when(query.executeAndFetchFirst(User.class)).thenReturn(user);

        assertEquals(user, UserService.byLogin("username"));
    }

    @Test
    void testByLoginFailed() {
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.executeAndFetchFirst(User.class)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> UserService.byLogin("username"));
    }

    @Test
    void testSave() {
        when(connection.createQuery(anyString())).thenReturn(query);
        User user = new User();
        when(query.bind(any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(connection);
        when(connection.getKey(Long.class)).thenReturn(1L);

        UserService.save(user);

        assertEquals(1L, user.getId());
    }

    @Test
    void testCreateTablespace() {
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);

        UserService.createTablespace("owner", "//path");

        verify(query).executeUpdate();
    }

    @Test
    void testByPublicKey() {
        when(connection.createQuery(anyString())).thenReturn(query);
        User user = mock(User.class);
        when(query.executeAndFetchFirst(User.class)).thenReturn(user);

        assertEquals(user, UserService.byPublicKey("key"));
    }

    @Test
    void testUserStoragePath() {
        assertEquals("path//owner", UserService.userStoragePath("owner"));
    }

    @Test
    void testInitUserStorage() {
        try(MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class);
            MockedStatic<CmdUtil> cmdUtil = mockStatic(CmdUtil.class)){
            UserService.initUserStorage("owner");
            fileUtils.verify(() -> FileUtils.forceMkdir(any()), times(3));
            cmdUtil.verify(() -> CmdUtil.exec(any()), never());
        }
    }

    @Test
    void testStorageSize() {
        try(MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)){
            fileUtils.when(() -> FileUtils.sizeOfDirectory(any())).thenReturn(1L);
            assertEquals(1L, UserService.storageSize("owner"));
        }
    }

    @Test
    void testNewDefaultUser() {
        User user = UserService.newDefaultUser();
        assertNotNull(user.getUsername());
        assertNotNull(user.getRole());
        assertNotNull(user.getDbPassword());
        assertNotNull(user.getPublicKey());
        assertNotNull(user.getPrivateKey());
        assertNotNull(user.getToken());
    }

    @Test
    void testByToken() {
        when(connection.createQuery(anyString())).thenReturn(query);
        User user = mock(User.class);
        when(query.executeAndFetchFirst(User.class)).thenReturn(user);

        assertEquals(user, UserService.byToken("username"));
    }

    @Test
    void testByTokenFailed() {
        when(connection.createQuery(anyString())).thenReturn(query);
        when(query.executeAndFetchFirst(User.class)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> UserService.byToken("username"));
    }
}