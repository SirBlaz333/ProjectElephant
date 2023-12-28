package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.MailService;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.ExceptionUtils;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.model.DbUserService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static edu.sumdu.tss.elephant.controller.RegistrationController.BASIC_PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class RegistrationControllerTest {
    private static MockedStatic<UserService> userService;
    private static MockedStatic<DbUserService> dbUserService;
    private static MockedStatic<MailService> mailService;
    private static MockedStatic<Keys> keys;
    private Context context;
    private Map<String, Object> model;
    private User user;

    @BeforeAll
    static void setUpAll() {
        userService = mockStatic(UserService.class);
        dbUserService = mockStatic(DbUserService.class);
        keys = Mockito.mockStatic(Keys.class);

        keys.when(() -> Keys.get("DEFAULT_LANG")).thenReturn("EN");
        keys.when(() -> Keys.get("EMAIL.FROM")).thenReturn("from");
        keys.when(() -> Keys.get("EMAIL.HOST")).thenReturn("localhost");
        keys.when(() -> Keys.get("EMAIL.PORT")).thenReturn("5432");
        keys.when(() -> Keys.get("EMAIL.SSL")).thenReturn("something");
        keys.when(() -> Keys.get("EMAIL.USER")).thenReturn("emailuser");
        keys.when(() -> Keys.get("EMAIL.PASSWORD")).thenReturn("emailpassowrd");

        mailService = mockStatic(MailService.class);
    }

    @AfterAll
    static void tearDownAll() {
        userService.close();
        dbUserService.close();
        mailService.close();
        keys.close();
    }

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        model = new HashMap<>();
        model.put("msg", new MessageBundle("EN"));
        user = new User();
        user.setUsername("user");
        when(context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY)).thenReturn(user);
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
        userService.when(UserService::newDefaultUser).thenReturn(user);
    }

    @Test
    void show() {
        RegistrationController.show(context);

        verify(context).render(any(), eq(model));
    }

    @Test
    void testCreateSuccessfulUserCreation() {
        Validator<String> loginValidator = mock(Validator.class);
        Validator<String> passwordValidator = mock(Validator.class);
        when(loginValidator.check(any(), anyString())).thenReturn(loginValidator);
        when(passwordValidator.check(any(), anyString())).thenReturn(passwordValidator);

        when(context.formParamAsClass("login", String.class)).thenReturn(loginValidator);
        when(context.formParamAsClass("password", String.class)).thenReturn(passwordValidator);
        when(loginValidator.get()).thenReturn("user");
        when(passwordValidator.get()).thenReturn("password");

        RegistrationController.create(context);

        assertEquals("user", user.getLogin());
        assertEquals(user.crypt("password"), user.getPassword());
        userService.verify(() -> UserService.save(user));
        userService.verify(() -> UserService.initUserStorage(user.getUsername()));
        verify(context).sessionAttribute(eq("currentUser"), eq(user));
        dbUserService.verify(() -> DbUserService.initUser(user.getUsername(), user.getDbPassword()));
        mailService.verify(() -> MailService.sendActivationLink(eq(user.getToken()), eq(user.getLogin()), any()));
        verify(context).redirect(HomeController.BASIC_PAGE);
    }

    @Test
    void testCreateDuplicateLoginException() {
        Validator<String> loginValidator = mock(Validator.class);
        Validator<String> passwordValidator = mock(Validator.class);
        when(loginValidator.check(any(), anyString())).thenReturn(loginValidator);
        when(passwordValidator.check(any(), anyString())).thenReturn(passwordValidator);

        when(context.formParamAsClass("login", String.class)).thenReturn(loginValidator);
        when(context.formParamAsClass("password", String.class)).thenReturn(passwordValidator);
        when(loginValidator.get()).thenReturn("user");
        when(passwordValidator.get()).thenReturn("password");

        try (MockedStatic<ExceptionUtils> exceptionUtils = Mockito.mockStatic(ExceptionUtils.class)) {
            exceptionUtils.when(() -> ExceptionUtils.isSQLUniqueException(any())).thenReturn(true);
            when(context.sessionAttribute(Keys.LANG_KEY)).thenReturn("en");
            userService.when(() -> UserService.save(user)).thenThrow(RuntimeException.class);

            RegistrationController.create(context);

            verify(context).sessionAttribute(eq(Keys.ERROR_KEY), eq("Login (email) already taken"));
            verify(context).redirect(BASIC_PAGE);
        }
    }

    @Test
    void testUserConfirmationValidToken() {
        when(context.pathParam("token")).thenReturn("validToken");
        userService.when(() -> UserService.byToken(anyString())).thenReturn(user);

        RegistrationController.userConformation(context);

        assertEquals(UserRole.BASIC_USER.getValue(), user.getRole());
        userService.verify(() -> UserService.save(user));
        verify(context).sessionAttribute(Keys.SESSION_CURRENT_USER_KEY, user);
        verify(context).sessionAttribute(Keys.INFO_KEY, "Email approved");
        verify(context).redirect(HomeController.BASIC_PAGE);
    }

    @Test
    void testUserConfirmationInvalidToken() {
        when(context.pathParam("token")).thenReturn("invalidToken");
        userService.when(() -> UserService.byToken("invalidToken")).thenReturn(null);
        assertThrows(NotFoundException.class, () -> RegistrationController.userConformation(context));
    }

    @Test
    void testResendUserConfirmationSuccessfulResend() {
        RegistrationController.resendUserConformation(context);

        verify(context).sessionAttribute(Keys.INFO_KEY, "Resend conformation email");
        verify(context).redirect(HomeController.BASIC_PAGE);
    }

    @Test
    void register() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        RegistrationController registrationController = new RegistrationController(mockApp);

        Mockito.verify(mockApp).get(eq(BASIC_PAGE + "/confirm/{token}"), any());
        Mockito.verify(mockApp).get(eq(BASIC_PAGE + "/resend-confirm/"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
        Mockito.verify(mockApp).get(eq(BASIC_PAGE ), any(), eq(UserRole.ANYONE));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE), any(), eq(UserRole.ANYONE));
    }
}