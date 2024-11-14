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
        Socket clientSocketTwo = serverSocket.accept(); 

        DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
        DataInputStream dataInTwo = new DataInputStream(clientSocketTwo.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());
        DataOutputStream dataOutTwo = new DataOutputStream(clientSocketTwo.getOutputStream());

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

        Thread sendMessageToClientTwo = new Thread(){
            public void run() {
                while (true) {
                    try {
                        JarDrop.sendMessage(dataOutTwo, sc); 
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
                        System.out.println("\r[CLIENT ONE] " + JarDrop.getMessage(dataIn, ip));
                        System.out.print(JarDrop.USER_PROMPT);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread getMessageFromClientTwo = new Thread(){
            public void run() {
                while (true) {
                    try {
                        System.out.println("\r[CLIENT TWO] " + JarDrop.getMessage(dataInTwo, ip));
                        System.out.print(JarDrop.USER_PROMPT);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        getMessageFromClient.start();
        getMessageFromClientTwo.start();
        sendMessageToClient.start();
        sendMessageToClientTwo.start();

        getMessageFromClient.join();
        getMessageFromClientTwo.join();
        sendMessageToClient.join();
        sendMessageToClientTwo.join();

        dataIn.close();
        dataOut.close();
        sc.close();
        clientSocket.close();
        serverSocket.close();
    }
}
