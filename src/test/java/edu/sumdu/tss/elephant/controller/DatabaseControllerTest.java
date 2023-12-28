package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.model.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.sumdu.tss.elephant.controller.DatabaseController.BASIC_PAGE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseControllerTest {
    private static final String POINT = "point";
    private static final String DB_NAME = "database1";
    private static final String USERNAME = "user";
    private static MockedStatic<DatabaseService> databaseService;
    private static MockedStatic<LogService> logService;
    private static MockedStatic<ViewHelper> viewHelper;
    private Context context;
    private List<Database> list;
    private Map<String, Object> model;
    private Database database;

    @BeforeAll
    static void setUpAll() {
        databaseService = Mockito.mockStatic(DatabaseService.class);
        logService = Mockito.mockStatic(LogService.class);
        viewHelper = Mockito.mockStatic(ViewHelper.class);
    }

    @AfterAll
    static void tearDownAll() {
        databaseService.close();
        logService.close();
        viewHelper.close();
    }

    @BeforeEach
    void setUp() {
        context = Mockito.mock(Context.class);
        model = new HashMap<>();
        list = Mockito.mock(List.class);
        database = Mockito.mock(Database.class);
        Backup backup = Mockito.mock(Backup.class);
        User user = Mockito.mock(User.class);
        when(context.formParam(POINT)).thenReturn(POINT);
        when(context.sessionAttribute(Keys.DB_KEY)).thenReturn(database);
        when(context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY)).thenReturn(user);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
        when(database.getName()).thenReturn(DB_NAME);
        when(backup.getDatabase()).thenReturn(DB_NAME);
        when(backup.getPoint()).thenReturn(POINT);
        when(user.getUsername()).thenReturn(USERNAME);
        when(user.role()).thenReturn(UserRole.BASIC_USER);
    }

    @Test
    void showTest() {
        DatabaseController.show(context);
        verify(context).render(anyString(), eq(model));
    }

    @Test
    void createTest() {
        when(list.size()).thenReturn(0);
        databaseService.when(() -> DatabaseService.forUser(anyString())).thenReturn(list);

        DatabaseController.create(context);

        databaseService.verify(() -> DatabaseService.create(anyString(), anyString(), anyString()));
        logService.verify(() -> LogService.push(eq(context), anyString(), anyString()));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), anyString());
        verify(context).redirect(anyString());
    }

    @Test
    void createLimitTest() {
        when(list.size()).thenReturn(1000);
        databaseService.when(() -> DatabaseService.forUser(anyString())).thenReturn(list);

        DatabaseController.create(context);

        viewHelper.verify(() -> ViewHelper.softError(anyString(), eq(context)));
    }

    @Test
    void delete() {
        DatabaseController.delete(context);

        databaseService.verify(() -> DatabaseService.drop(database));
        logService.verify(() -> LogService.push(eq(context), anyString(), anyString()));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), anyString());
        verify(context).redirect(anyString());
    }

    @Test
    void register() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        DatabaseController databaseController = new DatabaseController(mockApp);

        Mockito.verify(mockApp).get(eq(BASIC_PAGE + "{database}"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "{database}/delete"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE ), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
    }
}