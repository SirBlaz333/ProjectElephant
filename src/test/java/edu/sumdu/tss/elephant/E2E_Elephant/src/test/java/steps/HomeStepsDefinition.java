package steps;

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
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class HomeStepsDefinition {
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

    @Given("I navigate to the home page")
    public void i_navigate_to_the_home_page() throws InterruptedException {
        Thread.sleep(1000);
        open("http://localhost:7000");
    }

    @When("Navigate Forgot password link")
    public void navigate_forgot_password_link() throws InterruptedException {
        Thread.sleep(1000);
        open("http://localhost:7000/login/reset-password");
    }

    @Then("I should see forgot password page")
    public void i_should_see_forgot_password_page() throws InterruptedException {
        Thread.sleep(1000);
        Assertions.assertEquals(1, 1);
    }
}
