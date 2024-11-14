/* Server.java */

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server {
    public static void serverMode (String ip, int port) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(port);
        Scanner sc = new Scanner(System.in);

        System.out.println("Listening for clients...");
        Socket clientSocket = serverSocket.accept();

        DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());

        Thread sendMessageToClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        JarDrop.sendMessage(dataOut, sc);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread getMessageFromClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        JarDrop.getMessage(dataIn, ip);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        getMessageFromClient.start();
        sendMessageToClient.start();

        getMessageFromClient.join();
        sendMessageToClient.join();

        dataIn.close();
        dataOut.close();
        sc.close();
        clientSocket.close();
        serverSocket.close();
    }
}
