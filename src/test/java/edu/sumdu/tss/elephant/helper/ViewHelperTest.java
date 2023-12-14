package edu.sumdu.tss.elephant.helper;

import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;


class ViewHelperTest {
    private Context context;
    private Map<String, Object> model;

    @BeforeEach
    void setUp() {
        model = new HashMap<>();
        context = Mockito.mock(Context.class);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
        when(context.path()).thenReturn(".");
        Keys.loadParams(new File("config.properties"));
        ViewHelper.defaultVariables(context);
    }

    @Test
    void userError() {
        int code = 404;
        String message = "Not Found";
        String icon = "error";
        String stacktrace = "Sample stacktrace";

        when(Keys.isProduction()).thenReturn(true);
        ViewHelper.userError(context, code, message, icon, stacktrace);
        verify(context).status(code);
        ArgumentCaptor<String> templatePathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).render(templatePathCaptor.capture(), modelCaptor.capture());

        assertEquals("/velocity/error.vm", templatePathCaptor.getValue());

        assertEquals(Integer.toString(code), model.get("code"));
        assertEquals(message, model.get("message"));
        assertEquals(icon, model.get("icon"));
        assertNull(model.get("stacktrace"));
    }

    @Test
    void testBreadcrumbWithExistingValueInSession() {
        List<String> existingBreadcrumb = new ArrayList<>();
        existingBreadcrumb.add("<a href='/previous'><ion-icon name=\"arrow-back-outline\"></ion-icon> Previous</a>");
        when(context.sessionAttribute(Keys.BREADCRUMB_KEY)).thenReturn(existingBreadcrumb);
        List<String> result = ViewHelper.breadcrumb(context);
        assertEquals(existingBreadcrumb, result);
    }

    @Test
    void testCleanupSession() {
        ViewHelper.cleanupSession(context);
        assertNull(model.get(Keys.MODEL_KEY));
        assertNull(model.get(Keys.DB_KEY));
        assertNull(model.get(Keys.BREADCRUMB_KEY));
    }

    @Test
    void testPager1Page() {
        String pager1 = ViewHelper.pager(1, 1);
        assertEquals("<nav>\n<ul class=\"pagination\"></ul>\n</nav>", pager1);
    }

    @Test
    void testPager3out5Page() {
        String pager2 = ViewHelper.pager(5, 3);
        assertEquals("<nav>\n<ul class=\"pagination\">" +
                "<li class=\"page-item\"><a class=\"page-link\" href=\"?offset=1\">1</a></li>\n" +
                "<li class=\"page-item\"><a class=\"page-link\" href=\"?offset=2\">2</a></li>\n" +
                "<li class=\"page-item active\"><a class=\"page-link\" href=\"#\">3</a></li>\n" +
                "<li class=\"page-item\"><a class=\"page-link\" href=\"?offset=4\">4</a></li>\n" +
                "</ul>\n</nav>", pager2);
    }
    @Test
    void testPager0Page() {
        String pager3 = ViewHelper.pager(0, 0);
        assertEquals("<nav>\n<ul class=\"pagination\"></ul>\n</nav>", pager3);
    }

    @Test
    void testSoftError() {
        String errorMessage = "Test Error Message";
        ViewHelper.softError(errorMessage, context);
        verify(context).sessionAttribute(Keys.ERROR_KEY, errorMessage);
        verify(context).redirect(anyString());
    }

    @Test
    void testRedirectBackWithRefererHeader() {
        String refererHeader = "/previous-page";
        when(context.header("Referer")).thenReturn(refererHeader);
        ViewHelper.redirectBack(context);
        verify(context).redirect(refererHeader);
    }

    @Test
    void testRedirectBackWithoutRefererHeader() {
        when(context.header("Referer")).thenReturn(null);
        ViewHelper.redirectBack(context);
        verify(context).redirect("/");
    }
}