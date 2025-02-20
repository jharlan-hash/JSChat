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
import java.util.Objects;

import com.jacksovern.Client.AES;
import com.jacksovern.Client.RSA;

public class Server {
    private static final List<ServerClient> clients = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static byte[] AESKeyBytes;

    private static void playWaitingAnimation() {
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

    private static void serverMode() throws IOException, InterruptedException {
        // Create a list to track all running threads
        List<Thread> activeThreads = new ArrayList<>();

        while (true) { // Keep checking for new clients
            // Check for any clients that don't have threads yet
            for (ServerClient client : new ArrayList<>(clients)) { // Create copy to avoid concurrent modification
                if (!activeThreads.stream().anyMatch(t -> t.getName().equals("ClientHandler-" + client.getClientID()) &&
                        t.isAlive())) { // this long line checks if a thread is already handling the client
                    // Create and start new thread for this client
                    Thread thread = handleClient(client);
                    thread.setName("ClientHandler-" + client.getClientID());
                    thread.start();
                    activeThreads.add(thread);
                    System.out.println("Started message handler for client " + client.getClientID());
                }
            }

            // Clean up any finished threads
            activeThreads.removeIf(thread -> !thread.isAlive());

            // Small delay to prevent busy-waiting
            Thread.sleep(100);
        }
    }

    private static Thread handleClient(ServerClient client) {
        return new Thread(() -> {
            try {
                while (true) {
                    DataInputStream dataIn = client.getDataIn();

                    int messageLength = dataIn.readInt(); // Read message length
                    if (messageLength <= 0) {
                        continue; // what does this do? keeping it in case it's important
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

                    Message message = new Message(messageBytes);

                    // Forward message to all other clients
                    for (ServerClient destClient : clients) {
                        if (!destClient.equals(client)) {
                            writeMessage(message, destClient.getDataOut());
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected");
            }
        });
    }

    private static byte[] readKeyBytes(DataInputStream dataIn, int length) throws IOException {
        byte[] keyBytes = new byte[length]; // Buffer for public key (usually 422 butes for RSA)
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

    private static void writeMessage(Message message, DataOutputStream dataOut) {
        try {
            // Send message length followed by message bytes
            dataOut.writeInt(message.getMessageLength());
            dataOut.write(message.getMessageBytes());
            dataOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server(int portNumber) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            System.out.println("Server started");

            AESKeyBytes = Objects.requireNonNull(AES.generateKey(128)).getEncoded();
            System.out.println("AES key generated");

            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening for clients...");

            // thread to continuously accept clients
            new Thread(() -> {
                try {
                    while (true) {
                        ServerClient client = new ServerClient(serverSocket.accept(), clients.size());
                        clients.add(client);

                        PublicKey clientKey = keyFactory
                                .generatePublic(new X509EncodedKeySpec(readKeyBytes(client.getDataIn(), 422)));
                        client.setPublicKey(clientKey);

                        System.out.println("Public key received from client " + client.getClientID());

                        client.getDataOut().write(RSA.encrypt(AESKeyBytes, client.getPublicKey()));
                        client.getDataOut().flush();

                        System.out.println("AES key sent to client " + client.getClientID());
                    }
                } catch (IOException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }).start();

            // playWaitingAnimation();
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
}
