import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void client_mode (String ip, int port) throws IOException, InterruptedException {
        Socket socket = new Socket(); // create a new socket
        Scanner sc = new Scanner(System.in); // for user input

        socket.connect(new InetSocketAddress(ip, port), 1000); // connect to server with 1000ms timeout
        System.out.println("Connection successful!");

        DataInputStream dataIn = new DataInputStream(socket.getInputStream()); // for reading data from server
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream()); // for sending data to server

        InetAddress addr = InetAddress.getByName(ip); // get the IP address
        String hostName = addr.getHostName(); // get the host name from the IP address


        Thread sendMessageToServer = new Thread(){ // create a new thread for sending messages to the server
            public void run() {
                while (true) {
                    try {
                        System.out.print("[you] ");
                        String messageSent = sc.nextLine();
                        dataOut.writeUTF(messageSent);

                        if (messageSent.contains("/exit")){
                            System.exit(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread getMessageFromServer = new Thread(){
            public void run() {
                boolean done = false;
                String messageReceived = "";

                while (!done) {
                    try {
                        messageReceived = dataIn.readUTF();
                        System.out.println("\r[" + hostName + "] " + messageReceived);
                        System.out.print("[you] ");
                        

                        if (messageReceived.contains("/exit")){
                            System.exit(0);
                        }
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
        socket.close();
        sc.close();
    }
}
