/* Server.java */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {
    public static void clientMode (String ip, int port) throws IOException, InterruptedException {
        Socket socket = new Socket();
        Scanner sc = new Scanner(System.in);

        socket.connect(new InetSocketAddress(ip, port), 10000); // connect to server with 10000ms timeout
        System.out.println("Connection successful!"); 

        DataInputStream dataIn = new DataInputStream(socket.getInputStream()); 
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        Thread getMessageFromServer = createThread(dataIn, dataOut, sc, "get");
        Thread sendMessageToServer = createThread(dataIn, dataOut, sc, "send");

        getMessageFromServer.join();
        sendMessageToServer.join();

        dataIn.close();
        dataOut.close();
        sc.close();
        socket.close();
    }

    public static Thread createThread(DataInputStream dataIn, DataOutputStream dataOut, Scanner sc, String getOrSend) {
        Thread getMessageFromClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        if (getOrSend.equals("send")) {
                            chatUtils.sendMessage(dataOut, sc);
                        } else if (getOrSend.equals("get")) {
                            System.out.println(chatUtils.getMessage(dataIn));
                            System.out.print(chatUtils.USER_PROMPT);
                        } else {
                            System.out.println("Invalid getOrSend argument.");
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        getMessageFromClient.start();

        return getMessageFromClient;
    }
}
