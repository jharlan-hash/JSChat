package com.jacksovern.Server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Socket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

class ServerClientTest {
    private ServerClient serverClient;
    private Socket mockSocket;
    private final int testClientId = 1;

    @BeforeEach
    void setUp() throws Exception {
        // Create mock socket with byte array streams
        mockSocket = mock(Socket.class);
        when(mockSocket.getInputStream())
            .thenReturn(new ByteArrayInputStream(new byte[0]));
        when(mockSocket.getOutputStream())
            .thenReturn(new ByteArrayOutputStream());
        
        serverClient = new ServerClient(mockSocket, testClientId);
    }

    @Test
    void testClientCreation() {
        assertNotNull(serverClient.getDataIn(), "DataInputStream should not be null");
        assertNotNull(serverClient.getDataOut(), "DataOutputStream should not be null");
        assertEquals(testClientId, serverClient.getClientID(), "Client ID should match constructor argument");
    }

    @Test
    void testPublicKeySetterGetter() throws Exception {
        // Generate a test public key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        serverClient.setPublicKey(keyPair.getPublic());
        assertEquals(keyPair.getPublic(), serverClient.getPublicKey(), "Public key getter should return set key");
    }
}
