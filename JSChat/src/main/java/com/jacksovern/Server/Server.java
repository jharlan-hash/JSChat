package com.jacksovern.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static int port = 1000;
    private static ServerClient client1;
    private static ServerClient client2;
    private static ServerSocket serverSocket;

    //public static void main() {
    //    try {
    //        serverMode(ServerUtils.portNumber);
    //    } catch (IOException e) {
    //        System.out.println("An error occurred while setting up the server.");
    //        e.printStackTrace();
    //    } catch (InterruptedException e) {
    //        System.out.println("An error occurred while running the server.");
    //        e.printStackTrace();
    //    } catch (Exception e) {
    //        System.out.println("An unknown error occurred.");
    //        e.printStackTrace();
    //    }
    //}

    public Server(int portNumber) {
        port = portNumber;
        try {
            System.out.println("Server started");
            serverSocket = new ServerSocket(port);
            System.out.println("listening for clients");
            client1 = new ServerClient(serverSocket.accept()); // this will eventually be a loop
            System.out.println("client 1 connected");
            client2 = new ServerClient(serverSocket.accept());
            System.out.println("client 2 connected");
            serverMode(port);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                client1.getDataIn().close();
                client1.getDataOut().close();
                client2.getDataIn().close();
                client2.getDataOut().close();
            } catch (Exception e) {
                System.out.println("error closing data lines, force closing");
                System.exit(1);
            }
        }
    }

    public static void serverMode(int port) throws IOException, InterruptedException {
        byte[] firstPublicKeyBytes = ServerUtils.readKeyBytes(client1.getDataIn(), 422);
        System.out.println("First public key read");

        byte[] secondPublicKeyBytes = ServerUtils.readKeyBytes(client2.getDataIn(), 422);
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

        ServerUtils.shutdown(client1.getDataIn(), client1.getDataOut(), client2.getDataIn(), client2.getDataOut(), null, null, null, serverSocket);
    }

    public static Thread createThread(DataInputStream dataIn, DataOutputStream dataOut) {
        Thread clientThread = new Thread() {
            public void run() {
                while (ServerUtils.serverIsRunning) {
                    try {
                        int messageLength = dataIn.readInt();
                        if (messageLength <= 0) {
                            continue;
                        }

                        byte[] message = new byte[messageLength];
                        int bytesRead = 0;
                        while (bytesRead < messageLength) {
                            int result = dataIn.read(message, bytesRead, messageLength - bytesRead);
                            if (result == -1) {
                                break;
                            }
                            bytesRead += result;
                        }

                        dataOut.writeInt(messageLength);
                        dataOut.write(message, 0, messageLength);
                        dataOut.flush();
                    } catch (IOException e) {
                        System.out.println("Client disconnected");
                        ServerUtils.serverIsRunning = false;
                        return;

                    }
                }
            }
        };

        return clientThread;
    }

}
