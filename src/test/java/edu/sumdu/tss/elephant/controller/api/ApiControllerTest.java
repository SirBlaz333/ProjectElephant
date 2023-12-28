package edu.sumdu.tss.elephant.controller.api;

import edu.sumdu.tss.elephant.controller.BackupController;
import edu.sumdu.tss.elephant.helper.Hmac;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.exception.BackupException;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.model.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static edu.sumdu.tss.elephant.controller.api.ApiController.BASIC_PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class ApiControllerTest {

    private static MockedStatic<UserService> userService;
    private static MockedStatic<DatabaseService> databaseService;
    private static MockedStatic<Keys> keys;
    private Context context;
    private Map<String, Object> model;
    private User user;

    @BeforeAll
    static void setUpAll() {
        userService = mockStatic(UserService.class);
        databaseService = mockStatic(DatabaseService.class);
        keys = Mockito.mockStatic(Keys.class);
    }

    @AfterAll
    static void tearDownAll() {
        userService.close();
        databaseService.close();
        keys.close();
    }

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        model = new HashMap<>();
        model.put("msg", new MessageBundle("EN"));
        user = new User();
        user.setLogin("user");
        user.setPrivateKey("privateKey");
        user.setPublicKey("publicKey");
        Database database = Mockito.mock(Database.class);
        when(database.getName()).thenReturn("database1");
        when(database.getOwner()).thenReturn("owner1");
        when(context.sessionAttribute(Keys.DB_KEY)).thenReturn("database1");
        when(context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY)).thenReturn(user);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
        when(context.path()).thenReturn("path");
        userService.when(() -> UserService.byPublicKey(any())).thenReturn(user);
        databaseService.when(() -> DatabaseService.activeDatabase(any(), any())).thenReturn(database);
    }

    @Test
    void testBackup() throws NoSuchAlgorithmException, InvalidKeyException {
        try (MockedStatic<BackupService> backupService = Mockito.mockStatic(BackupService.class)) {
            when(context.header("signature")).thenReturn(Hmac.calculate("path", "privateKey"));
            when(context.pathParam("point")).thenReturn("backupPoint");

            ApiController.backup(context);

            verify(context, never()).json(any());
            verify(context).status(204);
        }
    }

    @Test
    void testFailedBackupUserIsNotValidated() {
        when(context.header("signature")).thenReturn("");
        when(context.pathParam("point")).thenReturn("backupPoint");

        ApiController.backup(context);

        verify(context).json(any());
    }

    @Test
    void testFailedBackup() throws NoSuchAlgorithmException, InvalidKeyException {
        try (MockedStatic<BackupService> backupService = Mockito.mockStatic(BackupService.class)) {
            when(context.header("signature")).thenReturn(Hmac.calculate("path", "privateKey"));
            when(context.pathParam("point")).thenReturn("backupPoint");
            backupService.when(() -> BackupService.perform(any(), any(), any())).thenThrow(BackupException.class);

            ApiController.backup(context);

            verify(context).json(any());
        }
    }

    @Test
    void testRestore() throws NoSuchAlgorithmException, InvalidKeyException {
        try (MockedStatic<BackupService> backupService = Mockito.mockStatic(BackupService.class)) {
            when(context.header("signature")).thenReturn(Hmac.calculate("path", "privateKey"));
            when(context.pathParam("point")).thenReturn("backupPoint");

            ApiController.restore(context);

            verify(context, never()).json(any());
            verify(context).status(204);
        }
    }

    @Test
    void testFailedRestore() throws NoSuchAlgorithmException, InvalidKeyException {
        try (MockedStatic<BackupService> backupService = Mockito.mockStatic(BackupService.class)) {
            when(context.header("signature")).thenReturn(Hmac.calculate("path", "privateKey"));
            when(context.pathParam("point")).thenReturn("backupPoint");
            backupService.when(() -> BackupService.restore(any(), any(), any())).thenThrow(BackupException.class);

            ApiController.restore(context);

            verify(context).json(any());
        }
    }

    @Test
    void registerTest() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        ApiController apiController = new ApiController(mockApp);

        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "database/{name}/create/{point}"), any());
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "database/{name}/reset/{point}"), any());
    }
}