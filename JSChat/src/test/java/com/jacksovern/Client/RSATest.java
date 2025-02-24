// RSATest.java
package com.jacksovern.Client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;

class RSATest {
    private KeyPair keyPair;
    private final String testMessage = "Test message for RSA";

    @BeforeEach
    void setUp() throws Exception {
        keyPair = RSA.generateRSAKeyPair();
    }

    @Test
    void testKeyPairGeneration() {
        assertNotNull(keyPair, "Generated key pair should not be null");
        assertNotNull(keyPair.getPublic(), "Public key should not be null");
        assertNotNull(keyPair.getPrivate(), "Private key should not be null");
        assertEquals("RSA", keyPair.getPublic().getAlgorithm(), "Key algorithm should be RSA");
    }

    @Test
    void testEncryptionDecryption() throws Exception {
        // Encrypt test message
        byte[] encrypted = RSA.encrypt(testMessage, keyPair.getPublic());
        assertNotNull(encrypted, "Encrypted data should not be null");
        
        // Decrypt test message
        byte[] decrypted = RSA.decrypt(encrypted, keyPair.getPrivate());
        assertEquals(testMessage, new String(decrypted), "Decrypted message should match original");
    }

    @Test
    void testByteArrayEncryption() {
        byte[] testData = "Test byte array".getBytes();
        byte[] encrypted = RSA.encrypt(testData, keyPair.getPublic());
        assertNotNull(encrypted, "Encrypted byte array should not be null");
    }
}
