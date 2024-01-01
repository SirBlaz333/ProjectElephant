package com.e2e.e2e_elephant;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.openqa.selenium.chrome.ChromeOptions;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class MainPageTest {
    com.e2e.e2e_elephant.MainPage mainPage = new com.e2e.e2e_elephant.MainPage();

    @BeforeAll
    public static void setUpAll() {
        Configuration.browserSize = "1280x800";
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @BeforeEach
    public void setUp() {
        // Fix the issue https://github.com/SeleniumHQ/selenium/issues/11750
        Configuration.browserCapabilities = new ChromeOptions().addArguments("--remote-allow-origins=*");
    }

    @Test
    public void createUsers() {
        for (int i = 300; i < 301; i++)
        {
            String login = "user" + i + "@example.com";
            String password = "Password1!";

            open("http://localhost:7000/registration");

            mainPage.inputEmail.click();
            mainPage.inputEmail.setValue(login);

            mainPage.inputPassword.click();
            mainPage.inputPassword.setValue(password);

            mainPage.inputConformation.click();
            mainPage.inputConformation.setValue(password);

            mainPage.buttonSign.click();

            open("http://localhost:7000/profile");

            mainPage.buttonOutlinePrimary.click();

            mainPage.linkLogout.click();
        }
    }
}
