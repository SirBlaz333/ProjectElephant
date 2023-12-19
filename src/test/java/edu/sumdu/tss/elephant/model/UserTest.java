package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void password() {
        User user = new User();
        user.setLogin("myLogin");

        user.password("myPassword");

        String actual = user.getPassword();
        String expected = "26e608b0476ebf1b13ec6c30571fecadcb5abaf928de12d282c710887aa892b0d2d2f188b640eafe198371f3b9602104";

        assertEquals(expected, actual);
    }

    @Test
    void role() {
        User user = new User();
        user.setRole(UserRole.ADMIN.getValue());

        UserRole result = user.role();

        assertEquals(UserRole.ADMIN, result);
    }

    @Test
    void crypt() {
        User user = new User();
        user.setLogin("myLogin");

        String result = user.crypt("mySource");

        assertEquals("1ca76e989175d3761206f6d57b542a7e984f3762c8d3245c22fedf7a29056b34299d9fd963efca9093cc7d69fc0e1e81", result);
    }

    @Test
    void resetToken() {
        User user = new User();

        user.resetToken();

        assertNotNull(user.getToken());
        assertEquals(15, user.getToken().length());
    }

    @Test
    void gettersAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setLogin("testLogin");
        user.setPassword("testPassword");
        user.setUsername("testUsername");
        user.setDbPassword("testDbPassword");
        user.setRole(UserRole.BASIC_USER.getValue());
        user.setPrivateKey("testPrivateKey");
        user.setPublicKey("testPublicKey");
        user.setToken("testToken");
        user.setLanguage("testLanguage");

        assertEquals(1L, user.getId());
        assertEquals("testLogin", user.getLogin());
        assertEquals("testPassword", user.getPassword());
        assertEquals("testUsername", user.getUsername());
        assertEquals("testDbPassword", user.getDbPassword());
        assertEquals(UserRole.BASIC_USER.getValue(), user.getRole());
        assertEquals("testPrivateKey", user.getPrivateKey());
        assertEquals("testPublicKey", user.getPublicKey());
        assertEquals("testToken", user.getToken());
        assertEquals("testLanguage", user.getLanguage());
    }

    @Test
    void equals() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(1L);
        User user3 = new User();
        user3.setId(2L);

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertNotEquals(user2, user3);
    }

    @Test
    void canEqual() {
        User user = new User();

        assertTrue(user.canEqual(new User()));
        assertFalse(user.canEqual(new Object()));
    }

    @Test
    void hashCodeTest() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(1L);
        User user3 = new User();
        user3.setId(2L);

        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void toStringTest() {
        User user = new User();
        user.setId(1L);
        user.setLogin("testLogin");
        user.setPassword("testPassword");
        user.setUsername("testUsername");
        user.setDbPassword("testDbPassword");
        user.setRole(UserRole.BASIC_USER.getValue());
        user.setPrivateKey("testPrivateKey");
        user.setPublicKey("testPublicKey");
        user.setToken("testToken");
        user.setLanguage("testLanguage");

        String expected = "User(id=1, login=testLogin, password=testPassword, " +
                "username=testUsername, dbPassword=testDbPassword, role=2, " +
                "privateKey=testPrivateKey, publicKey=testPublicKey, " +
                "token=testToken, language=testLanguage)";
        assertEquals(expected, user.toString());
    }
}
