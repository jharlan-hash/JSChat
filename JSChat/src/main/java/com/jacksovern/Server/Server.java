package com.jacksovern.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private static int port = 1000;
    private static ServerClient client1;
    private static ServerClient client2;
    private static ServerSocket serverSocket;

    public Server(int portNumber) {
        port = portNumber;
        try {
            System.out.println("Server started");
            serverSocket = new ServerSocket(port);
            System.out.println("listening for clients");
            client1 = new ServerClient(serverSocket.accept(), 0); // this will eventually be a loop
            System.out.println("client 1 connected");
            client2 = new ServerClient(serverSocket.accept(), 1);
            System.out.println("client 2 connected");

            serverMode(1000);

            //Thread thread1 = new Thread() {
            //    public void run() {
            //        try {
            //            handleClient(client1);
            //        } catch (IOException e) {
            //            e.printStackTrace();
            //        }
            //    }
            //};
            //
            //Thread thread2 = new Thread() {
            //    public void run() {
            //        try {
            //            handleClient(client2);
            //        } catch (IOException e) {
            //            e.printStackTrace();
            //        }
            //    }
            //};
            //
            //thread1.start();
            //thread2.start();
            //
            //thread1.join();
            //thread2.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                client1.closeAll();
                client2.closeAll();
            } catch (Exception e) {
                System.out.println("error closing data streams, force closing");
                System.exit(1);
            }
        }
    }

    public void handleClient(ServerClient client) throws IOException {
        int id = client.getClientID();
        byte[] AESKeyBytes = new byte[384];
        ServerClient thisClient;
        ServerClient otherClient;

        System.out.println("client id: " + id);

        if (id == 0) {
            thisClient = client1;
            otherClient = client2;
        } else {
            thisClient = client2;
            otherClient = client1;

        }

        byte[] publicKeyBytes = readKeyBytes(thisClient.getDataIn(), 422);
        System.out.println("Public key read");

        otherClient.getDataOut().write(publicKeyBytes); // swapping public key bytes
        //
        if (id == 0) { // client must be first - reading AES key
            if (client.getDataIn().read(AESKeyBytes) < 384) {
                System.out.println("AES key not received in full.");
            }
        } else { // client must be second - aes key not needed so it is discarded
            client.getDataIn().readNBytes(384);
        }

        thisClient.getDataOut().write(publicKeyBytes);

        otherClient.getDataOut().write(AESKeyBytes);
        thisClient.getDataOut().flush();

        Thread thread = createThread(thisClient.getDataIn(), otherClient.getDataOut());

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void serverMode(int port) throws IOException, InterruptedException {
        byte[] firstPublicKeyBytes = readKeyBytes(client1.getDataIn(), 422);
        System.out.println("First public key read");

        byte[] secondPublicKeyBytes = readKeyBytes(client2.getDataIn(), 422);
        System.out.println("Second public key read");

        client2.getDataOut().write(firstPublicKeyBytes);
        client1.getDataOut().write(secondPublicKeyBytes);

        byte[] AESKeyBytes = new byte[384];
        if (client1.getDataIn().read(AESKeyBytes) < 384) {
            System.out.println("AES key not received in full.");
        }

        client2.getDataIn().readNBytes(384); // discarding unused AES key

        client1.getDataOut().write(AESKeyBytes);
        client2.getDataOut().write(AESKeyBytes);
        client1.getDataOut().flush();
        client2.getDataOut().flush();

        Thread thread1 = createThread(client1.getDataIn(), client2.getDataOut());
        Thread thread2 = createThread(client2.getDataIn(), client1.getDataOut());

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

    }

    public static byte[] readKeyBytes(DataInputStream dataIn, int length) throws IOException {
        byte[] keyBytes = new byte[length]; // public key is usually 422 bytes long

        int p = 0;
        while (p < keyBytes.length) {
            int read = dataIn.read(keyBytes);
            if (read == -1) {
                throw new RuntimeException("Premature end of stream in ServerUtils");
            }
            p += read;
        }

        return keyBytes;
    }

    public static Thread createThread(DataInputStream dataIn, DataOutputStream dataOut) {
        Thread clientThread = new Thread() {
            public void run() {
                boolean serverIsRunning = true;
                while (serverIsRunning) {
                    try {
                        int messageLength = dataIn.readInt();
                        if (messageLength <= 0) {
                            continue;
                        }

                        byte[] messageBytes = new byte[messageLength];
                        int bytesRead = 0;
                        while (bytesRead < messageLength) {
                            int result = dataIn.read(messageBytes, bytesRead, messageLength - bytesRead);
                            if (result == -1) {
                                break;
                            }
                            bytesRead += result;
                        }

                        writeMessage(new Message(messageBytes));
                    } catch (IOException e) {
                        System.out.println("Client disconnected");
                        serverIsRunning = false;
                        return;

                    }
                }
            }

            public void writeMessage(Message message) {
                try {
                    dataOut.writeInt(message.getMessageLength()); // equivalent to message.length
                    dataOut.write(message.getMessageBytes());
                    dataOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        return clientThread;
    }

}
