package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.model.DbUserService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static edu.sumdu.tss.elephant.controller.ProfileController.BASIC_PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    private static MockedStatic<UserService> userService;
    private static MockedStatic<DbUserService> dbUserService;
    private static MockedStatic<Keys> keys;
    private Context context;
    private Map<String, Object> model;
    private User user;

    @BeforeAll
    static void setUpAll() {
        userService = mockStatic(UserService.class);
        dbUserService = mockStatic(DbUserService.class);
        keys = Mockito.mockStatic(Keys.class);
    }

    @AfterAll
    static void tearDownAll() {
        userService.close();
        dbUserService.close();
        keys.close();
    }

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        model = new HashMap<>();
        model.put("msg", new MessageBundle("EN"));
        user = new User();
        user.setLogin("user");
        when(context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY)).thenReturn(user);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
    }

    @Test
    void show() {
        ProfileController.show(context);

        verify(context).render(any(), eq(model));
    }

    @Test
    void language() {
        when(context.queryParam("lang")).thenReturn("EN");

        ProfileController.language(context);

        assertEquals(user.getLanguage(), "EN");
        userService.verify(() -> UserService.save(user));
        verify(context).redirect(BASIC_PAGE);
    }

    @Test
    void testResetDbPassword() {
        when(context.formParam("db-password")).thenReturn("newDbPassword");

        ProfileController.resetDbPassword(context);

        assertEquals(user.getDbPassword(), "newDbPassword");
        userService.verify(() -> UserService.save(user));
        dbUserService.verify(() -> DbUserService.dbUserPasswordReset(user.getUsername(), "newDbPassword"));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), eq("DB user password was changed"));
        verify(context).redirect(eq(BASIC_PAGE));
    }

    @Test
    void resetWebPassword() {
        when(context.formParam("web-password")).thenReturn("newWebPassword");

        ProfileController.resetWebPassword(context);

        assertEquals(user.getPassword(), user.crypt("newWebPassword"));
        userService.verify(() -> UserService.save(user));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), eq("Web user password was changed"));
        verify(context).redirect(eq(BASIC_PAGE));
    }

    @Test
    void resetApiPassword() {
        ProfileController.resetApiPassword(context);

        assertNotNull(user.getPublicKey());
        assertNotNull(user.getPublicKey());
        userService.verify(() -> UserService.save(user));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), anyString());
        verify(context).redirect(eq(BASIC_PAGE));
    }

    @Test
    void upgradeUser() {
        when(context.formParam("role")).thenReturn(String.valueOf(UserRole.ADMIN));

        ProfileController.upgradeUser(context);

        assertEquals(UserRole.ADMIN.getValue(), user.getRole());
        userService.verify(() -> UserService.save(user));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), anyString());
        verify(context).redirect(eq(BASIC_PAGE));
    }

    @Test
    void removeSelf() {
        ProfileController.removeSelf(context);

        dbUserService.verify(() -> DbUserService.dropUser(user.getUsername()));
        verify(context).redirect(eq("/"));
    }

    @Test
    void register() {

        Javalin mockApp = Mockito.mock(Javalin.class);

        ProfileController profileController = new ProfileController(mockApp);

        Mockito.verify(mockApp).get(eq(BASIC_PAGE + "/lang"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "/reset-password"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "/reset-db"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "/reset-api"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "/upgrade"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "/remove-self"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).get(eq(BASIC_PAGE), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
    }
}