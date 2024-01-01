package com.e2e.e2e_elephant;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class MainPage {
    public SelenideElement inputEmail = $("#email");

    public SelenideElement inputPassword = $("input[aria-label='Password']");

    public SelenideElement inputConformation = $("#conformation");

    public SelenideElement buttonSign = $("button[class^='w-100']");

    public SelenideElement inputFloating = $("#floatingInput");

    public SelenideElement inputFloatingPassword = $("input[aria-label='Password']");

    public SelenideElement buttonSign2 = $("button[class^='w-100']");

    public SelenideElement buttonOutlinePrimary = $("html > body > div > div > main > div:nth-of-type(5) > div > div:nth-of-type(1) > form > div > div:nth-of-type(2) > button");

    public SelenideElement linkLogout = $("a[class$='text-nowrap']");
}
