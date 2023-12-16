package edu.sumdu.tss.elephant.helper;

import edu.sumdu.tss.elephant.helper.enums.Lang;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class MailServiceTest {

    private String token;
    private String user;
    private Lang lang;

    @BeforeEach
    void setUp() {
        token = "token";
        user = "user";
        lang = Lang.EN;
        Keys.loadParams(new File("config.properties"));
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