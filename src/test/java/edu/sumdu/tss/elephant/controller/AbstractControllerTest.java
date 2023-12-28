package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AbstractControllerTest {
    private Context context;

    @BeforeEach
    void setUp() {
        context = Mockito.mock(Context.class);
    }

    @Test
    void currentUserTest(){
        User defaultUser = new User();
        when(context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY)).thenReturn(defaultUser);
        User user =  AbstractController.currentUser(context);
        assertEquals(defaultUser, user);
    }

    @Test
    void currentDBTest() {
        Database defaultDatabase = new Database();
        when(context.sessionAttribute(Keys.DB_KEY)).thenReturn(defaultDatabase);
        Database database =  AbstractController.currentDB(context);
        assertEquals(defaultDatabase, database);
    }

    @Test
    void currentModelTest() {
        Map<String, Object> defaultModel = new HashMap<>();
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(defaultModel);
        Map<String, Object> model =  AbstractController.currentModel(context);
        assertEquals(defaultModel, model);
    }

    @Test
    void currentMessagesWithModel() {
        Map<String, Object> model = new HashMap<>();
        MessageBundle defaultMessageBundle = new MessageBundle("EN");
        model.put("msg", defaultMessageBundle);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
        MessageBundle messageBundle =  AbstractController.currentMessages(context);
        assertEquals(defaultMessageBundle, messageBundle);
    }

    @Test
    void currentMessagesWithoutModel() {
        try (MockedStatic<Keys> keys = Mockito.mockStatic(Keys.class)) {
            when(Keys.get("DEFAULT_LANG")).thenReturn("EN");
            Map<String, Object> model = new HashMap<>();
            when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
            MessageBundle messageBundle =  AbstractController.currentMessages(context);
            assertEquals(new MessageBundle("EN"), messageBundle);
        }
    }
}