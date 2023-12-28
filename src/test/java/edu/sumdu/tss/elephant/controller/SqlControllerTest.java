package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.model.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sql2o.Sql2o;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static edu.sumdu.tss.elephant.controller.SqlController.BASIC_PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class SqlControllerTest {
    private static MockedStatic<DatabaseService> databaseService;
    private static MockedStatic<ViewHelper> viewHelper;
    private static MockedStatic<Keys> keys;
    private static MockedStatic<DBPool> dbPool;
    private Context context;
    private Map<String, Object> model;
    private User user;

    @BeforeAll
    static void setUpAll() {
        databaseService = mockStatic(DatabaseService.class);
        viewHelper = Mockito.mockStatic(ViewHelper.class);
        keys = Mockito.mockStatic(Keys.class);
        keys.when(() -> Keys.get("DB.NAME")).thenReturn("db");
        keys.when(() -> Keys.get("DB.URL")).thenReturn("localhost");
        keys.when(() -> Keys.get("DB.PORT")).thenReturn("5432");
        dbPool = Mockito.mockStatic(DBPool.class);
    }

    @AfterAll
    static void tearDownAll() {
        databaseService.close();
        keys.close();
        viewHelper.close();
        dbPool.close();
    }

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        model = new HashMap<>();
        model.put("msg", new MessageBundle("EN"));
        user = new User();
        user.setUsername("user");
        user.setRole(UserRole.BASIC_USER.getValue());
        Database database = Mockito.mock(Database.class);
        when(database.getName()).thenReturn("database1");
        when(context.sessionAttribute(Keys.DB_KEY)).thenReturn(database);
        when(context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY)).thenReturn(user);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
    }

    @Test
    void showTest() {
        when(context.sessionAttribute("query")).thenReturn("query");
        viewHelper.when(() -> ViewHelper.breadcrumb(context)).thenReturn(new ArrayList<>());

        SqlController.show(context);

        assertEquals("query", model.get("query"));
        verify(context).render(anyString(), eq(model));
    }

    @Test
    void testRunSuccessfulQueryExecution() throws SQLException {
        Database database = mock(Database.class);
        when(database.getName()).thenReturn("database1");
        when(database.getOwner()).thenReturn("owner1");
        databaseService.when(() -> DatabaseService.activeDatabase(anyString(), anyString())).thenReturn(database);

        when(context.formParam("query")).thenReturn("SELECT * FROM your_table");
        when(context.pathParam("database")).thenReturn("database");

        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        Sql2o sql2o = mock(Sql2o.class);
        org.sql2o.Connection sConnection = mock(org.sql2o.Connection.class);
        dbPool.when(() -> DBPool.getConnection(anyString())).thenReturn(sql2o);
        when(sql2o.open()).thenReturn(sConnection);
        when(sConnection.getJdbcConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.execute(anyString())).thenReturn(true);
        when(mockStatement.getResultSet()).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("column1");
        when(mockMetaData.getColumnLabel(2)).thenReturn("column2");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("column1")).thenReturn("value1");
        when(mockResultSet.getString("column2")).thenReturn("value2");

        SqlController.run(context);

        verify(context).result(anyString());
        verify(mockConnection).close();
        verify(mockStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    void testRunEmptyQuery() throws SQLException {
        Database database = mock(Database.class);
        when(database.getName()).thenReturn("database1");
        when(database.getOwner()).thenReturn("owner1");
        databaseService.when(() -> DatabaseService.activeDatabase(anyString(), anyString())).thenReturn(database);

        when(context.formParam("query")).thenReturn("SELECT * FROM your_table");
        when(context.pathParam("database")).thenReturn("database");

        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        Sql2o sql2o = mock(Sql2o.class);
        org.sql2o.Connection sConnection = mock(org.sql2o.Connection.class);
        dbPool.when(() -> DBPool.getConnection(anyString())).thenReturn(sql2o);
        when(sql2o.open()).thenReturn(sConnection);
        when(sConnection.getJdbcConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.execute(anyString())).thenReturn(false);

        SqlController.run(context);

        verify(context).result(anyString());
        verify(mockConnection).close();
        verify(mockStatement).close();
    }

    @Test
    void testRunSqlException() throws SQLException {
        Database database = mock(Database.class);
        when(database.getName()).thenReturn("database1");
        when(database.getOwner()).thenReturn("owner1");
        databaseService.when(() -> DatabaseService.activeDatabase(anyString(), anyString())).thenReturn(database);

        when(context.formParam("query")).thenReturn("INVALID SQL");
        when(context.pathParam("database")).thenReturn("testDB");

        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        Sql2o sql2o = mock(Sql2o.class);
        org.sql2o.Connection sConnection = mock(org.sql2o.Connection.class);
        dbPool.when(() -> DBPool.getConnection(anyString())).thenReturn(sql2o);
        when(sql2o.open()).thenReturn(sConnection);
        when(sConnection.getJdbcConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.execute(anyString())).thenThrow(new SQLException("Syntax error"));

        SqlController.run(context);

        verify(context).result("<strong style='color: red;'>Syntax error</strong>");
        verify(mockConnection).close();
        verify(mockStatement).close();
    }

    @Test
    void register() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        SqlController sqlController = new SqlController(mockApp);

        Mockito.verify(mockApp).get(eq(BASIC_PAGE), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE), any());
    }
}