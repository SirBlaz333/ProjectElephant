package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.TableService;
import io.javalin.Javalin;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sql2o.data.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.sumdu.tss.elephant.controller.TableController.BASIC_PAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TableControllerTest {
    private static MockedStatic<TableService> tableService;
    private static MockedStatic<ViewHelper> viewHelper;
    private static MockedStatic<Keys> keys;
    private Context context;
    private Map<String, Object> model;

    @BeforeAll
    static void setUpAll() {
        tableService = Mockito.mockStatic(TableService.class);
        viewHelper = Mockito.mockStatic(ViewHelper.class);
        keys = Mockito.mockStatic(Keys.class);
    }

    @AfterAll
    static void tearDownAll() {
        tableService.close();
        keys.close();
        viewHelper.close();
    }

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        model = new HashMap<>();
        Database database = Mockito.mock(Database.class);
        when(database.getName()).thenReturn("database1");
        when(context.sessionAttribute(Keys.DB_KEY)).thenReturn(database);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
    }

    @Test
    void testIndex() {
        Table mockTables = Mockito.mock(Table.class);
        when(TableService.list(anyString())).thenReturn(mockTables);

        TableController.index(context);

        assertEquals(mockTables, model.get("tables"));
        verify(context).render(anyString(), eq(model));
    }

    @Test
    void testPreviewTable() {
        when(context.pathParam("table")).thenReturn("tableName");

        Validator<Integer> limitValidator = mock(Validator.class);
        Validator<Integer> offsetValidator = mock(Validator.class);
        when(limitValidator.check(any(), anyString())).thenReturn(limitValidator);
        when(offsetValidator.check(any(), anyString())).thenReturn(offsetValidator);

        when(context.queryParamAsClass("limit", Integer.class)).thenReturn(limitValidator);
        when(context.queryParamAsClass("offset", Integer.class)).thenReturn(offsetValidator);
        when(limitValidator.getOrDefault(anyInt())).thenReturn(1);
        when(offsetValidator.getOrDefault(anyInt())).thenReturn(1);

        Table mockTable = mock(Table.class);
        when(TableService.byName(anyString(), anyString(), anyInt(), anyInt())).thenReturn(mockTable);

        when(TableService.getTableSize(anyString(), anyString())).thenReturn(50);

        List<String> list = new ArrayList<>();
        viewHelper.when(() -> ViewHelper.breadcrumb(context)).thenReturn(list);
        viewHelper.when(() -> ViewHelper.pager(anyInt(), anyInt())).thenReturn("mockPager");

        TableController.preview_table(context);

        assertEquals(mockTable, model.get("table"));
        assertEquals("mockPager", model.get("pager"));
        assertEquals(2, list.size());
        verify(context).render(anyString(), eq(model));
    }

    @Test
    void register() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        TableController tableController = new TableController(mockApp);

        Mockito.verify(mockApp).get(eq(BASIC_PAGE), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).get(eq(BASIC_PAGE + "{table}"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
    }
}