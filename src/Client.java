/* Client.java */

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void clientMode (String ip, int port) throws IOException, InterruptedException {
        Socket socket = new Socket();
        Scanner sc = new Scanner(System.in);

        socket.connect(new InetSocketAddress(ip, port), 1000); // connect to server with 1000ms timeout
        System.out.println("Connection successful!"); 

        DataInputStream dataIn = new DataInputStream(socket.getInputStream()); 
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        Thread sendMessageToServer = new Thread(){ // create a new thread for sending messages to the server
            public void run() {
                while (true) {
                    try {
                        JarDrop.sendMessage(dataOut, sc); // send message to server
                    } catch (Exception e) { 
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread getMessageFromServer = new Thread(){
            public void run() {
                while (true) {
                    try {
                        JarDrop.getMessage(dataIn, ip);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        getMessageFromServer.start();
        sendMessageToServer.start();
        getMessageFromServer.join();
        sendMessageToServer.join();

        dataIn.close();
        dataOut.close();
        sc.close();
        socket.close();
    }
}
