package edu.sumdu.tss.elephant.middleware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CSRFTokenServiceTest {

    @Test
    void generateTokenTest() {
        String token = CSRFTokenService.getSiteWideToken();
        assertEquals("test token", token);
    }

    @Test
    void generateTokenTest2() {
        String token = CSRFTokenService.generateToken("123");
        assertNotNull(token);
    }

    @Test
    void generateTokenTest3() {
        assertTrue(CSRFTokenService.validateToken(CSRFTokenService.generateToken("123"),"123"));
    }

    @Test
    void generateTokenTest4() {
        assertFalse(CSRFTokenService.validateToken(CSRFTokenService.generateToken("1234"),"123"));
    }
}