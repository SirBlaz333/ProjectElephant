package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import io.javalin.http.Context;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LogServiceTest {

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
    void testPush() {
        Context context = mock(Context.class);
        User user = mock(User.class);
        when(user.getLogin()).thenReturn("username");
        when(context.ip()).thenReturn("ip");
        when(context.sessionAttribute(anyString())).thenReturn(user);
        when(connection.createQuery(anyString())).thenReturn(query);

        LogService.push(context, "database", "message");
        verify(query).executeUpdate();
    }

}