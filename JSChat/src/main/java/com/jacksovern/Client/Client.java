package com.jacksovern.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyPair;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    private static final int AES_KEY_LENGTH = 384;

    public static void main(String[] args) {
        new Client("localhost", 1000);
    }

    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Socket socket;
    private KeyPair keyPair;

    private SecretKey AESKey;


    public Client(String serverIP, int port) {
        try {
            this.socket = new Socket();
            socket.connect(new InetSocketAddress(serverIP, port), 0);

            this.dataIn = new DataInputStream(socket.getInputStream());
            this.dataOut = new DataOutputStream(socket.getOutputStream());

            createKeys();

            System.out.println("Connected to server at " + serverIP + ":" + port);

            MessageSender sender = new MessageSender(dataOut, AESKey);
            MessageReceiver receiver = new MessageReceiver(dataIn, AESKey);

            Thread senderThread = sendThread(sender);
            Thread receiverThread = receiveThread(receiver);

            senderThread.start();
            receiverThread.start();

            senderThread.join();
            receiverThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Thread receiveThread(MessageReceiver receiver) {
        return new Thread(() -> {
            while (true) {
                try {
                    String message = receiver.getMessage();
                    if (message == null) {
                        closeAll();
                        break;
                    } else {
                        System.out.println(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread sendThread(MessageSender sender) {
        return new Thread(() -> {
            while (true) {
                try {
                    String message = sender.getMessage();

                    if (message.equals("/exit")) {
                        sender.send("Client disconnected.");
                        closeAll();
                        break;
                    }

                    if (!message.startsWith("/")) { // do not send commands to server
                        sender.send(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void closeAll() {
        try {
            dataIn.close();
            dataOut.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to generate a keypair, send it to the server, and recieve the AES key.
     */
    private void createKeys() {
        // Generate and send RSA keypair
        keyPair = RSA.generateRSAKeyPair();
        try {
            // Send public key to server
            dataOut.write(keyPair.getPublic().getEncoded());

            byte[] AESKeyBytes = new byte[AES_KEY_LENGTH];
            if (dataIn.read(AESKeyBytes) < AES_KEY_LENGTH) {
                System.out.println("AES key not received in full.");
            }

            AESKey = new SecretKeySpec(RSA.decrypt(AESKeyBytes, keyPair.getPrivate()), "AES");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
