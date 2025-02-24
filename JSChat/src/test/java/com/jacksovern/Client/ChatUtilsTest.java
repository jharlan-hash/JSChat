// ChatUtilsTest.java
package com.jacksovern.Client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

class ChatUtilsTest {
    private DataInputStream testDataIn;
    private DataOutputStream testDataOut;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        testDataOut = new DataOutputStream(outputStream);
    }

    @Test
    void testReceiveEncryptedMessage() throws Exception {
        // Create test message
        byte[] testMessage = "Test message".getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(testMessage.length);
        dos.write(testMessage);
        
        // Create DataInputStream with test data
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        testDataIn = new DataInputStream(bais);
        
        // Test receiving message
        byte[] received = ChatUtils.receiveEncryptedMessage(testDataIn);
        assertArrayEquals(testMessage, received, "Received message should match sent message");
    }

    @Test
    void testReadKeyBytes() throws Exception {
        // Create test key bytes
        byte[] testKeyBytes = new byte[422];
        ByteArrayInputStream bais = new ByteArrayInputStream(testKeyBytes);
        testDataIn = new DataInputStream(bais);
        
        byte[] received = ChatUtils.readKeyBytes(testDataIn, 422);
        assertEquals(422, received.length, "Received key bytes should be correct length");
    }
}
