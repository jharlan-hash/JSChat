package com.jacksovern.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import com.jacksovern.Client.AES;
import com.jacksovern.Client.RSA;

public class Server {
    private static int port = 1000;
    private static List<ServerClient> clients = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static byte[] AESKeyBytes;

    /**
     * @param portNumber
     */
    public Server(int portNumber) {
        port = portNumber;

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            System.out.println("Server started");

            AESKeyBytes = AES.generateKey(128).getEncoded();
            System.out.println("AES key generated");

            serverSocket = new ServerSocket(port);
            System.out.println("Listening for clients...");

            // thread to continuously accept clients
            new Thread(() -> {
                try {
                    while (true) {
                        clients.add(new ServerClient(serverSocket.accept(), clients.size()));
                        ServerClient client = clients.getLast();

                        PublicKey clientKey = keyFactory
                                .generatePublic(new X509EncodedKeySpec(readKeyBytes(client.getDataIn(), 422)));
                        client.setPublicKey(clientKey);

                        System.out.println("Public key received from client " + client.getClientID());
                    }
                } catch (IOException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }).start();

            waitForClients();
            serverMode();

        } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Closing server...");
                serverSocket.close();
                for (ServerClient client : clients) {
                    client.closeAll();
                }
            } catch (Exception e) {
                System.out.println("Error closing data streams, force closing");
                System.exit(1);
            }
        }
    }

    private static void waitForClients() {
        String[] frames = {
            "Waiting for clients   ",
            "Waiting for clients.  ",
            "Waiting for clients.. ",
            "Waiting for clients..."
        };
        int ANIMATION_DELAY = 250; // milliseconds

        int frameIndex = 0;

        while (clients.size() < 2) {
            System.out.print("\r" + frames[frameIndex]);
            frameIndex = (frameIndex + 1) % frames.length; // Loop through frames

            try {
                Thread.sleep(ANIMATION_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                break;
            }
        }

        // Clear the waiting message
        System.out.print("\r" + " ".repeat(frames[0].length()) + "\r"); // very proud of this line
    }

    // TODO: change this to make it work with arbitrary number of clients
    public static void serverMode() throws IOException, InterruptedException {
        PublicKey firstPublicKey = clients.get(0).getPublicKey();
        PublicKey secondPublicKey = clients.get(1).getPublicKey();

        // Share the AES key with both clients
        clients.get(0).getDataOut().write(RSA.encrypt(AESKeyBytes, firstPublicKey));
        clients.get(1).getDataOut().write(RSA.encrypt(AESKeyBytes, secondPublicKey));
        clients.get(0).getDataOut().flush();
        clients.get(1).getDataOut().flush();
        System.out.println("AES key distributed to both clients");

        // Start message forwarding threads between clients
        Thread thread1 = createThread(clients.get(0).getDataIn(), clients.get(1).getDataOut());
        Thread thread2 = createThread(clients.get(1).getDataIn(), clients.get(0).getDataOut());

        System.out.println("Starting message forwarding threads...");
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
        System.out.println("Message forwarding threads finished");
    }

    /**
     * @param dataIn
     * @param length
     * @return byte array
     * @throws IOException
     *                     This method reads & returns the key bytes from the client
     */
    public static byte[] readKeyBytes(DataInputStream dataIn, int length) throws IOException {
        byte[] keyBytes = new byte[length]; // Buffer for public key
        int p = 0;
        while (p < keyBytes.length) {
            int read = dataIn.read(keyBytes, p, keyBytes.length - p);
            if (read == -1) {
                throw new RuntimeException("Premature end of stream in ServerUtils");
            }
            p += read;
        }
        System.out.println("Successfully read key bytes");
        return keyBytes;
    }

    public static Thread createThread(DataInputStream dataIn, DataOutputStream dataOut) {
        return new Thread(() -> {
            boolean serverIsRunning = true;
            while (serverIsRunning) {
                try {
                    // Read message length
                    int messageLength = dataIn.readInt();
                    if (messageLength <= 0) {
                        continue;
                    }
                    // Read message data
                    byte[] messageBytes = new byte[messageLength];
                    int bytesRead = 0;
                    while (bytesRead < messageLength) {
                        int result = dataIn.read(messageBytes, bytesRead, messageLength - bytesRead);
                        if (result == -1) {
                            break;
                        }
                        bytesRead += result;
                    }
                    System.out.println("Forwarding message of length: " + messageLength);

                    // Forward message to the other client
                    writeMessage(new Message(messageBytes), dataOut);
                } catch (IOException e) {
                    System.out.println("Client disconnected");
                    serverIsRunning = false;
                }
            }
        });
    }

    private static void writeMessage(Message message, DataOutputStream dataOut) {
        try {
            // Send message length followed by message bytes
            dataOut.writeInt(message.getMessageLength());
            dataOut.write(message.getMessageBytes());
            dataOut.flush();
            System.out.println("Message sent successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
