package edu.sumdu.tss.elephant.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HmacTest {

    @Test
    void testCalculateWithKnownDataAndKey() throws NoSuchAlgorithmException, InvalidKeyException {
        String data = "example data";
        String key = "secret";
        String expectedHmac = "e5d81d46d981c7142c551751db60999ac1936a783beab53ef8b9a14aa62e7b3739850707b04fa7405656d79e4fcde2a3";

        assertEquals(expectedHmac, Hmac.calculate(data, key));
    }

    @Test
    void testDifferentDataSameKey() throws Exception {
        String data1 = "data1";
        String data2 = "data2";
        String key = "secret";

        String hmac1 = Hmac.calculate(data1, key);
        String hmac2 = Hmac.calculate(data2, key);

        assertNotEquals(hmac1, hmac2);
    }

    @Test
    void testInvalidKeyException() {
        String data = "example data";
        String key = ""; // Empty key

        // Expect IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> Hmac.calculate(data, key));
    }

}
