package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.model.Backup;
import edu.sumdu.tss.elephant.model.BackupService;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.sumdu.tss.elephant.controller.BackupController.BASIC_PAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackupControllerTest {

    private static final String POINT = "point";
    private static final String DB_NAME = "database1";
    private static final String USERNAME = "user";
    private static MockedStatic<BackupService> backupService;
    private static MockedStatic<ViewHelper> viewHelper;
    private Context context;
    private List<Backup> list;
    private Map<String, Object> model;

    @BeforeAll
    static void setUpAll() {
        backupService = Mockito.mockStatic(BackupService.class);
        viewHelper = Mockito.mockStatic(ViewHelper.class);
    }

    @AfterAll
    static void tearDownAll() {
        backupService.close();
        viewHelper.close();
    }

    @BeforeEach
    void setUp() {
        context = Mockito.mock(Context.class);
        list = Mockito.mock(List.class);
        model = new HashMap<>();
        Database database = Mockito.mock(Database.class);
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
        backupService.when(() -> BackupService.byName(DB_NAME, POINT)).thenReturn(backup);
        backupService.when(() -> BackupService.list(DB_NAME)).thenReturn(list);
    }

    @Test
    void restoreTest() {
        BackupController.restore(context);

        backupService.verify(() -> BackupService.restore(USERNAME, DB_NAME, POINT));
        verify(context).sessionAttribute(Keys.INFO_KEY, "Restore performed successfully");
        verify(context).redirect(BASIC_PAGE.replace("{database}", DB_NAME));
    }

    @Test
    void createTest() {
        when(list.size()).thenReturn(0);

        BackupController.create(context);

        backupService.verify(() -> BackupService.perform(USERNAME, DB_NAME, POINT));
        verify(context).sessionAttribute(Keys.INFO_KEY, "Backup created successfully");
        verify(context).redirect(BASIC_PAGE.replace("{database}", DB_NAME));
    }

    @Test
    void createMaxConnectionTest() {
        when(list.size()).thenReturn(1000);

        BackupController.create(context);

        viewHelper.verify(() -> ViewHelper.softError(anyString(), eq(context)));
    }

    @Test
    void createEmptyPointTest() {
        when(list.size()).thenReturn(0);
        when(context.formParam(POINT)).thenReturn("");

        BackupController.create(context);

        viewHelper.verify(() -> ViewHelper.softError(anyString(), eq(context)));
    }

    @Test
    void deleteTest() {
        BackupController.delete(context);

        backupService.verify(() -> BackupService.delete(USERNAME, DB_NAME, POINT));
        verify(context).redirect(BASIC_PAGE.replace("{database}", DB_NAME));
    }

    @Test
    void indexTest() {
        viewHelper.when(() -> ViewHelper.breadcrumb(context)).thenReturn(anyList());

        BackupController.index(context);

        assertEquals(list, model.get("points"));
        viewHelper.verify(() -> ViewHelper.breadcrumb(context));
        verify(context).render(anyString(), eq(model));
    }

    @Test
    void registerTest() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        BackupController backupController = new BackupController(mockApp);

        Mockito.verify(mockApp).get(eq(BASIC_PAGE), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "{point}/create"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "{point}/reset"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "{point}/delete"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
    }
}