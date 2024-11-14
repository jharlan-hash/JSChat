/* Server.java */

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server {
    public static void serverMode (int port) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(port);
        Scanner sc = new Scanner(System.in);

        System.out.println("Listening for clients...");
        Socket clientSocket = serverSocket.accept(); 
        DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());

        System.out.println("Client connected");

        Socket clientSocketTwo = serverSocket.accept(); 
        DataInputStream dataInTwo = new DataInputStream(clientSocketTwo.getInputStream());
        DataOutputStream dataOutTwo = new DataOutputStream(clientSocketTwo.getOutputStream());

        System.out.println("Second client connected");
        dataOut.writeUTF("\rClient connected");

        String hostName = clientSocket.getInetAddress().getHostName();
        String hostNameTwo = clientSocketTwo.getInetAddress().getHostName();

        Thread getMessageFromClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        dataOutTwo.writeUTF("\r[" + hostName + "] " + JarDrop.getMessage(dataIn).strip());
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
                        dataOut.writeUTF("\r[" + hostNameTwo + "] " + JarDrop.getMessage(dataInTwo).strip());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        getMessageFromClient.start();
        getMessageFromClientTwo.start();

        getMessageFromClient.join();
        getMessageFromClientTwo.join();

        dataIn.close();
        dataOut.close();
        sc.close();
        clientSocket.close();
        serverSocket.close();
    }
}
