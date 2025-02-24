// MessageTest.java
package com.jacksovern.Server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    @Test
    void testMessageCreation() {
        byte[] testData = "Test message".getBytes();
        Message message = new Message(testData);
        
        assertEquals(testData.length, message.getMessageLength(), "Message length should match input length");
        assertArrayEquals(testData, message.getMessageBytes(), "Message bytes should match input bytes");
    }
}

