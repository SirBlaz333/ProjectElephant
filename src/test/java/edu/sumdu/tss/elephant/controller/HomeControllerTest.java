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

import static edu.sumdu.tss.elephant.controller.HomeController.BASIC_PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomeControllerTest {

    private static final String POINT = "point";
    private static final String DB_NAME = "database1";
    private static final String USERNAME = "user";
    private static MockedStatic<DatabaseService> databaseService;
    private static MockedStatic<UserService> userService;
    private static MockedStatic<Keys> keys;
    private Context context;
    private Map<String, Object> model;
    private List<Database> list;

    @BeforeAll
    static void setUpAll() {
        databaseService = Mockito.mockStatic(DatabaseService.class);
        userService = Mockito.mockStatic(UserService.class);
        keys = Mockito.mockStatic(Keys.class);
    }

    @AfterAll
    static void tearDownAll() {
        databaseService.close();
        userService.close();
        keys.close();
    }

    @BeforeEach
    void setUp() {
        context = Mockito.mock(Context.class);
        model = new HashMap<>();
        list = Mockito.mock(List.class);
        Database database = Mockito.mock(Database.class);
        User user = Mockito.mock(User.class);
        when(context.formParam(POINT)).thenReturn(POINT);
        when(context.sessionAttribute(Keys.DB_KEY)).thenReturn(database);
        when(context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY)).thenReturn(user);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
        when(database.getName()).thenReturn(DB_NAME);
        when(user.getUsername()).thenReturn(USERNAME);
        when(user.role()).thenReturn(UserRole.BASIC_USER);
        when(list.size()).thenReturn(0);
        databaseService.when(() -> DatabaseService.forUser(anyString())).thenReturn(list);
        userService.when(() -> UserService.storageSize(anyString())).thenReturn(0L);
        keys.when(() -> Keys.get("DB.HOST")).thenReturn("localhost");
        keys.when(() -> Keys.get("DB.PORT")).thenReturn("5432");
    }

    @Test
    void showTest() {
        HomeController.show(context);

        assertEquals(model.get("bases"), list);
        assertEquals(model.get("sizeUsed"), 0L);
        assertEquals(model.get("sizeTotal"), UserRole.BASIC_USER.maxStorage());
        assertEquals(model.get("spacePercent"), 0L);
        assertEquals(model.get("host"), "localhost");
        assertEquals(model.get("port"), "5432");

        verify(context).render(anyString(), eq(model));
    }

    @Test
    void register() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        HomeController databaseController = new HomeController(mockApp);

        Mockito.verify(mockApp).get(eq("/"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).get(eq(BASIC_PAGE), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
    }

}