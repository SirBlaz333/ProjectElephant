package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class DbUserServiceTest {
    private static MockedStatic<UserService> userService;
    private static MockedStatic<Keys> keys;
    private static MockedStatic<DBPool> dbPool;
    private static Sql2o sql2o;
    private Connection connection;
    private Query query;

    @BeforeAll
    static void setUpAll() {
        userService = mockStatic(UserService.class);
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
        userService.close();
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
    public void testInit() throws Exception {
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);

        DbUserService.initUser("username", "password");
        userService.verify(() -> UserService.createTablespace(eq("username"), anyString()));
        verify(query).executeUpdate();
    }

    @Test
    public void testPasswordReset() throws Exception {
        when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);

        DbUserService.dbUserPasswordReset("username", "password");
        verify(query).executeUpdate();
    }

    @Test
    public void testDropUser() throws Exception {
        try(MockedStatic<CmdUtil> cmdUtil = mockStatic(CmdUtil.class)) {
            when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
            when(sql2o.beginTransaction()).thenReturn(connection);

            DbUserService.dropUser("username");
            verify(query).executeUpdate();
            cmdUtil.verify(() -> CmdUtil.exec(anyString()));
        }
    }
}