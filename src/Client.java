/* Client.java */

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

        socket.connect(new InetSocketAddress(ip, port), 0);
        System.out.println("Connection successful!");

        DataInputStream dataIn = new DataInputStream(socket.getInputStream()); 
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        Thread getMessageFromServer = createThread(dataIn, dataOut, sc, "get");
        Thread sendMessageToServer = createThread(dataIn, dataOut, sc, "send");

        getMessageFromServer.start();
        sendMessageToServer.start();

        getMessageFromServer.join();
        sendMessageToServer.join();


        dataIn.close();
        dataOut.close();
        sc.close();
        socket.close();
        System.exit(0);
    }

    public static Thread createThread(DataInputStream dataIn, DataOutputStream dataOut, Scanner sc, String mode) {
        Thread getMessageFromClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        if (mode.equals("send")) {
                            String message = ChatUtils.sendMessage(dataOut, sc);

                            if (message.equals(ChatUtils.EXIT_MESSAGE)){
                                break;
                            }

                        } else if (mode.equals("get")) {
                            String message = ChatUtils.getMessage(dataIn);

                            if (message == null || message.equals(ChatUtils.EXIT_MESSAGE)){
                                break;
                            }

                            System.out.println(message);
                            System.out.print(ChatUtils.USER_PROMPT);
                        } else {
                            System.out.println("Invalid mode argument.");
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        return getMessageFromClient;
    }
}
