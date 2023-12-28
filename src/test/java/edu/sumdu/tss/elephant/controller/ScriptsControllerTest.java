package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.*;
import edu.sumdu.tss.elephant.helper.exception.AccessRestrictedException;
import edu.sumdu.tss.elephant.helper.exception.HttpError500;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.sql.ScriptReader;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.model.*;
import io.javalin.Javalin;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.UploadedFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sql2o.Sql2o;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static edu.sumdu.tss.elephant.controller.ScriptsController.BASIC_PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ScriptsControllerTest {
    private static MockedStatic<UserService> userService;
    private static MockedStatic<ScriptService> scriptService;
    private static MockedStatic<ViewHelper> viewHelper;
    private static MockedStatic<Keys> keys;
    private static MockedStatic<FileUtils> fileUtils;
    private static MockedStatic<DBPool> dbPool;
    private Context context;
    private Map<String, Object> model;
    private User user;
    private Script script;

    @BeforeAll
    static void setUpAll() {
        userService = mockStatic(UserService.class);
        scriptService = mockStatic(ScriptService.class);
        viewHelper = Mockito.mockStatic(ViewHelper.class);
        fileUtils = Mockito.mockStatic(FileUtils.class);
        keys = Mockito.mockStatic(Keys.class);
        keys.when(() -> Keys.get("DB.NAME")).thenReturn("db");
        keys.when(() -> Keys.get("DB.URL")).thenReturn("localhost");
        keys.when(() -> Keys.get("DB.PORT")).thenReturn("5432");
        dbPool = Mockito.mockStatic(DBPool.class);
    }

    @AfterAll
    static void tearDownAll() {
        userService.close();
        scriptService.close();
        keys.close();
        viewHelper.close();
        fileUtils.close();
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
        when(context.pathParam("database")).thenReturn("database");
        when(context.pathParam("script")).thenReturn("1");
        userService.when(UserService::newDefaultUser).thenReturn(user);
        script = mock(Script.class);
        scriptService.when(() -> ScriptService.byId(1)).thenReturn(script);
        when(script.getDatabase()).thenReturn("database1");
    }

    @Test
    void testCreateSuccessfulScriptCreation() {
        when(ScriptService.list(anyString())).thenReturn(Collections.emptyList());

        UploadedFile mockFile = mock(UploadedFile.class);
        when(mockFile.getFilename()).thenReturn("testScript.sql");
        when(mockFile.getSize()).thenReturn(1024L); // Specify a suitable file size
        when(context.uploadedFile("file")).thenReturn(mockFile);

        Validator<String> descriptionValidator = mock(Validator.class);
        when(descriptionValidator.check(any(), anyString())).thenReturn(descriptionValidator);

        when(context.formParamAsClass("description", String.class)).thenReturn(descriptionValidator);
        when(descriptionValidator.getOrDefault(anyString())).thenReturn("Test script description");

        ScriptsController.create(context);

        scriptService.verify(() -> ScriptService.save(any()));
        verify(context).redirect(BASIC_PAGE.replace("{database}", "database1"));
    }

    @Test
    void testCreateScriptLimitReached() {
        List<Script> scripts = mock(List.class);
        when(scripts.size()).thenReturn(1000);
        when(ScriptService.list(anyString())).thenReturn(scripts);

        ScriptsController.create(context);

        viewHelper.verify(() -> ViewHelper.softError(eq("You limit reached"), eq(context)));
        verify(context, never()).redirect(anyString());
    }

    @Test
    void testShowValidScriptId() {
        when(script.getPath()).thenReturn("testPath");

        try {
            ScriptsController.show(context);
        } catch (Exception e) {
            assertEquals(e.getClass(), HttpError500.class);
        }
    }

    @Test
    void testShowAccessRestrictedException() {
        when(script.getDatabase()).thenReturn("otherDB");

        assertThrows(AccessRestrictedException.class, () -> ScriptsController.show(context));
    }

    @Test
    void testIndex() {
        List<Script> mockScripts = new ArrayList<>();
        Script mockScript1 = mock(Script.class);
        Script mockScript2 = mock(Script.class);
        mockScripts.add(mockScript1);
        mockScripts.add(mockScript2);
        when(ScriptService.list("database1")).thenReturn(mockScripts);

        List<String> mockBreadcrumb = mock(List.class);
        when(ViewHelper.breadcrumb(context)).thenReturn(mockBreadcrumb);

        ScriptsController.index(context);

        assertEquals(mockScripts, model.get("scripts"));
        verify(mockBreadcrumb).add("Scripts");
        verify(context).render(eq("/velocity/script/index.vm"), eq(model));
    }


    @Test
    void testRunSuccessfulExecution(@TempDir Path tempPath) throws SQLException, IOException {
        File scriptFile = Files.createTempFile(tempPath, "testScript", ".sql").toFile();
        PrintWriter writer = new PrintWriter(scriptFile);
        writer.println("SELECT * FROM your_table;");
        writer.close();

        when(script.getPath()).thenReturn(scriptFile.getAbsolutePath());

        Connection connection = mock(Connection.class);
        Sql2o sql2o = mock(Sql2o.class);
        org.sql2o.Connection sConnection = mock(org.sql2o.Connection.class);
        dbPool.when(() -> DBPool.getConnection(anyString())).thenReturn(sql2o);
        when(sql2o.open()).thenReturn(sConnection);
        when(sConnection.getJdbcConnection()).thenReturn(connection);

        ScriptReader scriptReader = mock(ScriptReader.class);
        when(scriptReader.readStatement()).thenReturn("SELECT * FROM your_table;", null);

        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT * FROM your_table;")).thenReturn(null);

        ScriptsController.run(context);

        assertEquals(List.of(new Pair<>("SELECT * FROM your_table", "ok"), new Pair<>("\r\n", "ok")), model.get("executeResults"));
        verify(context).render(anyString(), eq(model));
        scriptFile.deleteOnExit();
    }

    @Test
    void testRunFileNotFoundException() {
        when(script.getPath()).thenReturn("nonexistentPath");

        assertThrows(HttpError500.class, () -> ScriptsController.run(context));
    }

    @Test
    void testDeleteSuccessfulDeletion() {
        ScriptsController.delete(context);

        scriptService.verify(() -> ScriptService.destroy(script));
        verify(context).redirect(BASIC_PAGE.replace("{database}", "database1"));
    }

    @Test
    void testDeleteScriptNotFound() {
        when(script.getDatabase()).thenReturn("database2");

        assertThrows(NotFoundException.class, () -> ScriptsController.delete(context));
    }

    @Test
    void register() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        ScriptsController scriptsController = new ScriptsController(mockApp);

        Mockito.verify(mockApp).get(eq(BASIC_PAGE), any());
        Mockito.verify(mockApp).post(eq(BASIC_PAGE ), any());
        Mockito.verify(mockApp).get(eq(BASIC_PAGE + "{script}"), any());
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "{script}"), any());
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "{script}/delete"), any());
    }

}