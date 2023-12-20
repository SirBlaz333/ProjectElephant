package edu.sumdu.tss.elephant.helper;

import edu.sumdu.tss.elephant.helper.enums.Lang;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import javax.mail.MessagingException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class MailServiceTest {
    private static MockedStatic<Keys> mockedKeys;

    private String token;
    private String user;
    private Lang lang;

    @BeforeAll
    static void setUpAll() {
        mockedKeys = mockStatic(Keys.class);
        mockedKeys.when(() -> Keys.get("DEFAULT_LANG")).thenReturn("EN");
        mockedKeys.when(() -> Keys.get("EMAIL.FROM")).thenReturn("from");
        mockedKeys.when(() -> Keys.get("EMAIL.HOST")).thenReturn("localhost");
        mockedKeys.when(() -> Keys.get("EMAIL.PORT")).thenReturn("5432");
        mockedKeys.when(() -> Keys.get("EMAIL.SSL")).thenReturn("something");
        mockedKeys.when(() -> Keys.get("EMAIL.USER")).thenReturn("emailuser");
        mockedKeys.when(() -> Keys.get("EMAIL.PASSWORD")).thenReturn("emailpassowrd");
    }

    @AfterAll
    static void tearDownAll() {
        mockedKeys.close();
    }

    @BeforeEach
    void setUp() {
        token = "token";
        user = "user";
        lang = Lang.EN;
    }

    @Test
    void sendActivationLink() throws MessagingException {
        MailService.sendActivationLink(token, user, lang);
        assertTrue(Files.exists(Paths.get(MailService.FILE_NAME)));
    }

    @Test
    void sendResetLink() throws MessagingException {
        MailService.sendResetLink(token, user, lang);
        assertTrue(Files.exists(Paths.get(MailService.FILE_NAME)));
    }

    @AfterEach
    void tearDown() throws IOException {
        Path filePath = Paths.get(MailService.FILE_NAME);
        Files.deleteIfExists(filePath);
    }
}