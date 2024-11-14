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

        Thread getMessageFromClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        dataOutTwo.writeUTF("\r[CLIENT ONE] " + JarDrop.getMessage(dataIn, ip));
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
                        dataOut.writeUTF("\r[CLIENT TWO] " + JarDrop.getMessage(dataInTwo, ip).strip());
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
