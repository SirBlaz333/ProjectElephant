package edu.sumdu.tss.elephant.helper.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorHelperTest {

    @Test
    void isValidPassword() {
        assertTrue(ValidatorHelper.isValidPassword("Abcdefg1#")); // Valid password
        assertTrue(ValidatorHelper.isValidPassword("Test123@"));   // Valid password
        assertFalse(ValidatorHelper.isValidPassword("short1#"));    // Password too short
        assertFalse(ValidatorHelper.isValidPassword("noDigit#"));   // Missing number
        assertFalse(ValidatorHelper.isValidPassword("nouppercase1#"));   // Missing uppercase letter
        assertFalse(ValidatorHelper.isValidPassword("NoSpecial1"));    // Missing special character
        assertFalse(ValidatorHelper.isValidPassword("TooLongPassword12345678901#"));  // Password too long
    }

    @Test
    void isValidMail() {
        assertTrue(ValidatorHelper.isValidMail("test@example.com"));  // Valid email
        assertTrue(ValidatorHelper.isValidMail("user@mail.co"));       // Valid email
        assertTrue(ValidatorHelper.isValidMail("john.doe@gmail.com")); // Valid email
        assertFalse(ValidatorHelper.isValidMail("invalid-email"));     // Invalid email without '@' symbol
        assertFalse(ValidatorHelper.isValidMail("missing-at.com"));     // Invalid email without '@' symbol
        assertFalse(ValidatorHelper.isValidMail("user@missing-dot"));   // Invalid email without '.' symbol
        assertFalse(ValidatorHelper.isValidMail("user@invalid@dot.com"));// Invalid email with multiple '@' symbols
        assertFalse(ValidatorHelper.isValidMail("@."));// Invalid email
    }
}