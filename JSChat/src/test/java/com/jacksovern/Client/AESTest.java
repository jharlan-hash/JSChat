// AESTest.java
package com.jacksovern.Client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SecretKey;

class AESTest {
    private SecretKey testKey;
    private final String testMessage = "Hello, World!";

    @BeforeEach
    void setUp() {
        testKey = AES.generateKey(128);
    }

    @Test
    void testKeyGeneration() {
        assertNotNull(testKey, "Generated key should not be null");
        assertEquals("AES", testKey.getAlgorithm(), "Key algorithm should be AES");
        assertEquals(16, testKey.getEncoded().length, "Key length should be 16 bytes for AES-128");
    }

    @Test
    void testEncryptionDecryption() throws Exception {
        // Encrypt test message
        byte[] encrypted = AES.encrypt(testMessage, testKey);
        assertNotNull(encrypted, "Encrypted data should not be null");
        assertTrue(encrypted.length > 0, "Encrypted data should have length > 0");

        // Decrypt test message
        String decrypted = AES.decrypt(encrypted, testKey);
        assertEquals(testMessage, decrypted, "Decrypted message should match original");
    }

    @Test
    void testIVGeneration() {
        assertNotNull(AES.generateIv(), "Generated IV should not be null");
        assertEquals(16, AES.generateIv().getIV().length, "IV length should be 16 bytes");
    }
}
