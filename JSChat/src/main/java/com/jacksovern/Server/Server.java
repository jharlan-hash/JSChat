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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jacksovern.Message;
import com.jacksovern.Client.AES;
import com.jacksovern.Client.RSA;

public class Server {
    private static final List<ServerClient> clients = new CopyOnWriteArrayList<>();
    private static final List<Thread> activeClientThreads = new CopyOnWriteArrayList<>();
    private static ServerSocket serverSocket;
    private static byte[] AESKeyBytes;

    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    public static void main(String[] args) {
        int portNumber = 1000;

        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        }

        new Server(portNumber);
    }

    private static void handleClient(ServerClient client) {
        try {
            // Create and start new thread for this client
            Thread thread = createClientThread(client);
            thread.setName("ClientHandler-" + client.getClientID());
            thread.start();
            activeClientThreads.add(thread);
            System.out.println("Started message handler for client " + client.getClientID());
        } catch (Exception e) {
            System.out.println("Error starting message handler for client " + client.getClientID());
            e.printStackTrace();
        }
    }

    private static void acceptClient(ServerClient client) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            clients.add(client);

            PublicKey clientKey = keyFactory
                    .generatePublic(new X509EncodedKeySpec(readKeyBytes(client.getDataIn(), 422)));
            client.setPublicKey(clientKey);

            System.out.println("Public key received from client " + client.getClientID());

            byte[] encryptedAESKey = RSA.encrypt(AESKeyBytes, client.getPublicKey());

            client.getDataOut().write(encryptedAESKey);
            client.getDataOut().flush();

            handleClient(client);
            System.out.println("AES key sent to client " + client.getClientID());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private static Thread createClientThread(ServerClient client) {
        return new Thread(() -> {
            try {
                while (clients.size() > 0) {
                    DataInputStream dataIn = client.getDataIn();

                    int messageLength = dataIn.readInt(); // Read message length
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

                    Message message = new Message(messageBytes, client.getClientID());

                    // Forward message to all other clients
                    for (ServerClient destClient : new CopyOnWriteArrayList<>(clients)) {
                        if (!destClient.equals(client)) {
                            writeMessage(message, destClient.getDataOut());
                        }
                    }
                }
            } catch (IOException e) {
                client.closeAll();
                clients.remove(client);
                System.out.println("Client " + client.getClientID() + " disconnected");
            }
        });
    }

    private static byte[] readKeyBytes(DataInputStream dataIn, int length) throws IOException {
        byte[] keyBytes = new byte[length]; // Buffer for public key (usually 422 bytes for RSA)
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
            System.out.println("Server started");

            AESKeyBytes = Objects.requireNonNull(AES.generateKey(128)).getEncoded();
            System.out.println("AES key generated");

            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening for clients...");

            // continuously accept clients
            while (true) {
                acceptClient(new ServerClient(serverSocket.accept(), clients.size()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Closing server...");
                serverSocket.close();
                for (ServerClient client : new CopyOnWriteArrayList<>(clients)) {
                    client.closeAll();
                }
            } catch (Exception e) {
                System.out.println("Error closing data streams, force closing");
                System.exit(1);
            }
        }
    }
}
