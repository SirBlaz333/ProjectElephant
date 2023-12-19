package edu.sumdu.tss.elephant.helper.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidatorHelperTest {

    @Test
    void isValidPassword() {
        String validPassword = "Abcd@1234";
        String invalidPassword = "weakpassword";

        assertTrue(ValidatorHelper.isValidPassword(validPassword));
        assertFalse(ValidatorHelper.isValidPassword(invalidPassword));
    }

    @Test
    void isValidMail() {
        String validMail = "test@example.com";
        String invalidMail = "invalid-email";

        assertTrue(ValidatorHelper.isValidMail(validMail));
        assertFalse(ValidatorHelper.isValidMail(invalidMail));
    }
}