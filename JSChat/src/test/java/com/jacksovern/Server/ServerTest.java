package com.jacksovern.Server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.jacksovern.Client.RSA;
import com.jacksovern.Client.AES;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

class ServerIntegrationTest {
    private static final int TEST_PORT = 12345;
    private static Server server;
    private static Thread serverThread;
    private static final int RSA_KEY_LENGTH = 422; // Server expects exactly 422 bytes
    
    @BeforeAll
    static void startServer() {
        serverThread = new Thread(() -> {
            server = new Server(TEST_PORT);
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // Wait for server to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @AfterEach
    void cleanup() throws InterruptedException {
        Thread.sleep(1000);
    }
    
    @AfterAll
    static void stopServer() {
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
    }

    @Test
    @Timeout(10)
    void testClientConnection() throws Exception {
        TestClient client = new TestClient();
        assertTrue(client.getSocket().isConnected());
        client.close();
    }

    @Test
    @Timeout(10)
    void testKeyExchange() throws Exception {
        TestClient client = new TestClient();
        assertNotNull(client.getAesKey());
        client.close();
    }

    @Test
    @Timeout(10)
    void testMessageBroadcast() throws Exception {
        TestClient client1 = new TestClient();
        TestClient client2 = new TestClient();
        
        Thread.sleep(1000); // Allow clients to fully connect
        
        String testMessage = "Test message";
        byte[] encryptedMessage = AES.encrypt(testMessage, client1.getAesKey());
        
        // Send message from client1
        CountDownLatch sendLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                client1.getDataOut().writeInt(encryptedMessage.length);
                client1.getDataOut().write(encryptedMessage);
                client1.getDataOut().flush();
                sendLatch.countDown();
            } catch (IOException e) {
                fail("Failed to send message: " + e.getMessage());
            }
        }).start();
        
        assertTrue(sendLatch.await(5, TimeUnit.SECONDS), "Failed to send message within timeout");
        
        // Receive message on client2
        CountDownLatch receiveLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                int messageLength = client2.getDataIn().readInt();
                byte[] receivedMessage = new byte[messageLength];
                client2.getDataIn().readFully(receivedMessage);
                String decryptedMessage = AES.decrypt(receivedMessage, client2.getAesKey());
                assertEquals(testMessage, decryptedMessage);
                receiveLatch.countDown();
            } catch (Exception e) {
                fail("Failed to receive message: " + e.getMessage());
            }
        }).start();
        
        assertTrue(receiveLatch.await(5, TimeUnit.SECONDS), "Failed to receive message within timeout");
        
        client1.close();
        client2.close();
    }

    private static class TestClient {
        private final Socket socket;
        private final DataInputStream dataIn;
        private final DataOutputStream dataOut;
        private final SecretKey aesKey;
        
        public TestClient() throws Exception {
            socket = new Socket("localhost", TEST_PORT);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            
            // Generate and send RSA key pair
            KeyPair keyPair = RSA.generateRSAKeyPair();
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            
            // Ensure we're sending exactly 422 bytes
            if (publicKeyBytes.length != RSA_KEY_LENGTH) {
                byte[] paddedKey = new byte[RSA_KEY_LENGTH];
                System.arraycopy(publicKeyBytes, 0, paddedKey, 0, Math.min(publicKeyBytes.length, RSA_KEY_LENGTH));
                publicKeyBytes = paddedKey;
            }
            
            // Send public key in chunks to ensure all bytes are sent
            int totalSent = 0;
            while (totalSent < RSA_KEY_LENGTH) {
                int bytesToSend = Math.min(1024, RSA_KEY_LENGTH - totalSent);
                dataOut.write(publicKeyBytes, totalSent, bytesToSend);
                dataOut.flush();
                totalSent += bytesToSend;
            }
            
            // Read AES key
            byte[] encryptedAESKey = new byte[384];
            int totalRead = 0;
            while (totalRead < encryptedAESKey.length) {
                int bytesRead = dataIn.read(encryptedAESKey, totalRead, encryptedAESKey.length - totalRead);
                if (bytesRead == -1) {
                    throw new IOException("End of stream reached before AES key was fully read");
                }
                totalRead += bytesRead;
            }
            
            byte[] aesKeyBytes = RSA.decrypt(encryptedAESKey, keyPair.getPrivate());
            aesKey = new SecretKeySpec(aesKeyBytes, "AES");
        }
        
        public Socket getSocket() { return socket; }
        public DataInputStream getDataIn() { return dataIn; }
        public DataOutputStream getDataOut() { return dataOut; }
        public SecretKey getAesKey() { return aesKey; }
        
        public void close() throws IOException {
            dataIn.close();
            dataOut.close();
            socket.close();
        }
    }
}
