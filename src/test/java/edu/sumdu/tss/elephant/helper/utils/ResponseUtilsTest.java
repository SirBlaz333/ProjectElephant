package edu.sumdu.tss.elephant.helper.utils;

import edu.sumdu.tss.elephant.helper.ViewHelper;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseUtilsTest {
    private Context context;
    private Map<String, Object> model;

    @Test
    void success() {
        String message = "Operation successful";
        Object response = ResponseUtils.success(message);

        assertEquals(HashMap.class, response.getClass());
        assertEquals("Ok", ((HashMap<?, ?>) response).get("status"));
        assertEquals(message, ((HashMap<?, ?>) response).get("message"));
    }

    @Test
    void error() {
        String message = "Operation failed";
        Object response = ResponseUtils.error(message);

        assertEquals(HashMap.class, response.getClass());
        assertEquals("Error", ((HashMap<?, ?>) response).get("status"));
        assertEquals(message, ((HashMap<?, ?>) response).get("message"));
    }

    @Test
    public void testFlushFlash() {
        Context mockContext = mock(Context.class);
        model = new HashMap<>();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
        Map<String, Object> expectedMap = new HashMap<>();

        for (String key : ViewHelper.FLASH_KEY) {
            mockContext.sessionAttribute(key, "info");
            expectedMap.put(key, "info");
        }
        verify(mockContext, times(ViewHelper.FLASH_KEY.length)).sessionAttribute(keyCaptor.capture(), valueCaptor.capture());

        List<String> capturedKeys = keyCaptor.getAllValues();
        List<Object> capturedValues = valueCaptor.getAllValues();

        Map<String, Object> capturedMap = new HashMap<>();
        for (int i = 0; i < capturedKeys.size(); i++) {
            capturedMap.put(capturedKeys.get(i), capturedValues.get(i));
        }

        assertEquals(expectedMap, capturedMap);

        ResponseUtils.flush_flash(mockContext);

        for (String key : ViewHelper.FLASH_KEY) {
            assertNull(mockContext.sessionAttribute(key));
        }

    }
}