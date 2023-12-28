package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.MailService;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.helper.utils.ResponseUtils;
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

import static edu.sumdu.tss.elephant.controller.LoginController.BASIC_PAGE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoginControllerTest {
    private static MockedStatic<UserService> userService;
    private static MockedStatic<MessageBundle> messageBundle;
    private static MockedStatic<ViewHelper> viewHelper;
    private static MockedStatic<Keys> keys;
    private Context context;
    private Map<String, Object> model;

    @BeforeAll
    static void setUpAll() {
        userService = mockStatic(UserService.class);
        messageBundle = mockStatic(MessageBundle.class);
        viewHelper = Mockito.mockStatic(ViewHelper.class);
        keys = Mockito.mockStatic(Keys.class);
    }

    @AfterAll
    static void tearDownAll() {
        userService.close();
        messageBundle.close();
        viewHelper.close();
        keys.close();
    }

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        model = new HashMap<>();
        model.put("msg", new MessageBundle("EN"));
        when(context.sessionAttribute(Keys.MODEL_KEY)).thenReturn(model);
    }

    @Test
    void show() {
        LoginController.show(context);

        verify(context).render(any(), eq(model));
    }

    @Test
    void testCreateValidUserCredentialsRedirectsToHomePage() {
        Validator<String> loginValidator = mock(Validator.class);
        Validator<String> passwordValidator = mock(Validator.class);
        when(loginValidator.check(any(), anyString())).thenReturn(loginValidator);
        when(passwordValidator.check(any(), anyString())).thenReturn(passwordValidator);

        when(context.formParamAsClass(eq("login"), eq(String.class))).thenReturn(loginValidator);
        when(context.formParamAsClass(eq("password"), eq(String.class))).thenReturn(passwordValidator);
        when(loginValidator.get()).thenReturn("validUsername");
        when(passwordValidator.get()).thenReturn("validPassword");

        User mockUser = mock(User.class);
        when(mockUser.getPassword()).thenReturn("hashedValidPassword");
        when(mockUser.crypt(eq("validPassword"))).thenReturn("hashedValidPassword");
        userService.when(() -> UserService.byLogin(eq("validUsername"))).thenReturn(mockUser);

        LoginController.create(context);

        verify(context).sessionAttribute(eq(Keys.SESSION_CURRENT_USER_KEY), eq(mockUser));
        verify(context).redirect(eq(HomeController.BASIC_PAGE));
    }

    @Test
    void testCreateInvalidUserCredentialsSetsErrorAndRedirects() {
        Validator<String> loginValidator = mock(Validator.class);
        Validator<String> passwordValidator = mock(Validator.class);
        when(loginValidator.check(any(), anyString())).thenReturn(loginValidator);
        when(passwordValidator.check(any(), anyString())).thenReturn(passwordValidator);

        when(context.formParamAsClass(eq("login"), eq(String.class))).thenReturn(loginValidator);
        when(context.formParamAsClass(eq("password"), eq(String.class))).thenReturn(passwordValidator);
        when(loginValidator.get()).thenReturn("invalidUsername");
        when(passwordValidator.get()).thenReturn("invalidPassword");

        userService.when(() -> UserService.byLogin(eq("invalidUsername"))).thenThrow(new NotFoundException("User not found"));

        LoginController.create(context);

        verify(context, times(2)).sessionAttribute(eq(Keys.ERROR_KEY), any());
        //verify(context).redirect(HomeController.BASIC_PAGE,302);
    }

    @Test
    void testResetLinkPostMethodSuccessfulResetLinkSent() {
        keys.when(() -> Keys.get("DEFAULT_LANG")).thenReturn("EN");
        keys.when(() -> Keys.get("EMAIL.FROM")).thenReturn("from");
        keys.when(() -> Keys.get("EMAIL.HOST")).thenReturn("localhost");
        keys.when(() -> Keys.get("EMAIL.PORT")).thenReturn("5432");
        keys.when(() -> Keys.get("EMAIL.SSL")).thenReturn("something");
        keys.when(() -> Keys.get("EMAIL.USER")).thenReturn("emailuser");
        keys.when(() -> Keys.get("EMAIL.PASSWORD")).thenReturn("emailpassowrd");
        MockedStatic<MailService> mailService = Mockito.mockStatic(MailService.class);
        Validator<String> emailValidator = mock(Validator.class);
        when(emailValidator.check(any(), anyString())).thenReturn(emailValidator);
        when(context.method()).thenReturn("POST");
        when(context.formParamAsClass(eq("email"), eq(String.class))).thenReturn(emailValidator);
        when(emailValidator.get()).thenReturn("validEmail");

        User mockUser = mock(User.class);
        when(mockUser.getLanguage()).thenReturn("en");
        when(UserService.byLogin(eq("validEmail"))).thenReturn(mockUser);

        LoginController.resetLink(context);

        mailService.verify(() -> MailService.sendResetLink(any(), any(), any()));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), any());
        verify(context).redirect(eq(BASIC_PAGE), eq(302));
        mailService.close();
    }

    @Test
    void testResetLink_PostMethod_UserNotFound_SetsError() {
        Validator<String> emailValidator = mock(Validator.class);
        when(emailValidator.check(any(), anyString())).thenReturn(emailValidator);
        when(context.method()).thenReturn("POST");
        when(context.formParamAsClass(eq("email"), eq(String.class))).thenReturn(emailValidator);
        when(emailValidator.get()).thenReturn("nonexistentEmail");

        when(UserService.byLogin(eq("nonexistentEmail"))).thenThrow(new NotFoundException("User not found"));

        LoginController.resetLink(context);

        verify(context, times(2)).sessionAttribute(eq(Keys.ERROR_KEY), any());
        verify(context, never()).redirect(any(), anyInt());
    }

    @Test
    void testResetLinkGetMethodRendersTemplate() {
        when(context.method()).thenReturn("GET");

        LoginController.resetLink(context);

        verify(context, never()).redirect(any(), anyInt());
        verify(context).render(eq("/velocity/login/reset-link.vm"), anyMap());
    }

    @Test
    void testResetPasswordPostMethodSuccessfulPasswordReset() throws NotFoundException {
        Validator<String> tokenValidator = mock(Validator.class);
        Validator<String> passwordValidator = mock(Validator.class);
        when(tokenValidator.check(any(), anyString())).thenReturn(tokenValidator);
        when(passwordValidator.check(any(), anyString())).thenReturn(passwordValidator);

        when(context.method()).thenReturn("POST");
        when(context.formParamAsClass(eq("token"), eq(String.class))).thenReturn(tokenValidator);
        when(context.formParamAsClass(eq("password"), eq(String.class))).thenReturn(passwordValidator);
        when(tokenValidator.get()).thenReturn("validToken");
        when(passwordValidator.get()).thenReturn("newValidPassword");

        User mockUser = mock(User.class);
        userService.when(() -> UserService.byToken(eq("validToken"))).thenReturn(mockUser);

        // Act
        LoginController.resetPassword(context);

        // Assert
        verify(mockUser).password(eq("newValidPassword"));
        verify(mockUser).resetToken();
        userService.verify(() -> UserService.save(eq(mockUser)));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), any());
        verify(context).redirect(BASIC_PAGE);
    }

    @Test
    void testResetPasswordPostMethodInvalidTokenSetsErrorAndRedirects() throws NotFoundException {
        Validator<String> tokenValidator = mock(Validator.class);
        Validator<String> passwordValidator = mock(Validator.class);
        when(tokenValidator.check(any(), anyString())).thenReturn(tokenValidator);
        when(passwordValidator.check(any(), anyString())).thenReturn(passwordValidator);

        when(context.method()).thenReturn("POST");
        when(context.queryParamAsClass(eq("token"), eq(String.class))).thenReturn(tokenValidator);
        when(context.formParamAsClass(eq("token"), eq(String.class))).thenReturn(tokenValidator);
        when(context.formParamAsClass(eq("password"), eq(String.class))).thenReturn(passwordValidator);
        when(tokenValidator.get()).thenReturn("invalidToken");

        userService.when(() -> UserService.byToken(eq("invalidToken"))).thenThrow(new NotFoundException("User not found"));

        // Act
        LoginController.resetPassword(context);

        // Assert
        verify(context, times(2)).sessionAttribute(eq(Keys.ERROR_KEY), any());
        verify(context).redirect(BASIC_PAGE);
        verify(context).render(eq("/velocity/login/reset.vm"), anyMap());
    }

    @Test
    void testResetPasswordGetMethodRendersTemplate() {
        Validator<String> tokenValidator = mock(Validator.class);
        when(tokenValidator.check(any(), anyString())).thenReturn(tokenValidator);

        when(context.method()).thenReturn("GET");
        when(context.queryParamAsClass(eq("token"), eq(String.class))).thenReturn(tokenValidator);
        when(tokenValidator.getOrDefault(any())).thenReturn("validToken");

        LoginController.resetPassword(context);

        verify(context).render(eq("/velocity/login/reset.vm"), anyMap());
    }

    @Test
    void destroyTest() {
        MockedStatic<ResponseUtils> responseUtils = Mockito.mockStatic(ResponseUtils.class);

        LoginController.destroy(context);

        responseUtils.verify(() -> ResponseUtils.flush_flash(context));
        verify(context).sessionAttribute(Keys.SESSION_CURRENT_USER_KEY, null);
        verify(context).redirect(BASIC_PAGE);
        responseUtils.close();
    }

    @Test
    void testLangMethod() {
        when(context.pathParam("lang")).thenReturn("EN");
        keys.when(() -> Keys.get("DEFAULT_LANG")).thenReturn("EN");

        LoginController.lang(context);

        // Assert
        verify(context).sessionAttribute(eq(Keys.LANG_KEY), eq("EN"));
        viewHelper.verify(() -> ViewHelper.redirectBack(eq(context)));
    }

    @Test
    void testLangMethodInvalidLang() {
        when(context.pathParam("lang")).thenReturn("invalidLang");
        keys.when(() -> Keys.get("DEFAULT_LANG")).thenReturn("EN");

        LoginController.lang(context);

        verify(context).sessionAttribute(eq(Keys.ERROR_KEY), anyString());
        viewHelper.verify(() -> ViewHelper.redirectBack(eq(context)));
    }

    @Test
    void testLangMethodRuntimeException() {
        when(context.pathParam("lang")).thenThrow(new RuntimeException("Some error occurred"));
        keys.when(() -> Keys.get("DEFAULT_LANG")).thenReturn("EN");

        LoginController.lang(context);

        // Assert
        verify(context).sessionAttribute(eq(Keys.ERROR_KEY), anyString());
        viewHelper.verify(() -> ViewHelper.redirectBack(eq(context)));
    }

    @Test
    void register() {
        Javalin mockApp = Mockito.mock(Javalin.class);

        LoginController loginController = new LoginController(mockApp);

        Mockito.verify(mockApp).get(eq(BASIC_PAGE), any(),  eq(UserRole.ANYONE));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE), any(), eq(UserRole.ANYONE));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE  + "/reset-password"), any(), eq(UserRole.ANYONE));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE  + "/reset-password"), any(), eq(UserRole.ANYONE));
        Mockito.verify(mockApp).get(eq(BASIC_PAGE + "/reset"), any(), eq(UserRole.ANYONE));
        Mockito.verify(mockApp).post(eq(BASIC_PAGE + "/reset"), any(), eq(UserRole.ANYONE));
        Mockito.verify(mockApp).get(eq(BASIC_PAGE + "/lang/{lang}"), any(), eq(UserRole.ANYONE));
        Mockito.verify(mockApp).get(eq("/logout"), any(), eq(UserRole.UNCHEKED), eq(UserRole.BASIC_USER), eq(UserRole.PROMOTED_USER), eq(UserRole.ADMIN));
    }
}