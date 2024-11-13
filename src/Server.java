import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server {
    public static void server_mode (String ip, int port) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(port);
        Scanner sc = new Scanner(System.in);

        System.out.println("Listening for clients...");
        Socket clientSocket = serverSocket.accept();

        DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());

        InetAddress addr = InetAddress.getByName(ip);
        String hostName = addr.getHostName();


        Thread sendMessageToClient = new Thread(){
            public void run() {
                boolean done = false;

                while (!done) {
                    try {
                        if (!Thread.interrupted()){
                            System.out.print("[you] ");
                            String messageSent = sc.nextLine();
                            dataOut.writeUTF(messageSent);
                        } else {
                            System.out.println("sendMessageToClient interrupted");
                            done = true;
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread getMessageFromClient = new Thread(){
            public void run() {
                boolean done = false;
                String messageReceived = "";

                while (!done) {
                    try {
                        if (!messageReceived.contains("/exit")){
                            messageReceived = dataIn.readUTF();
                            System.out.println("\r[" + hostName + "] " + messageReceived);
                            System.out.print("[you] ");
                        } else {
                            System.out.println("\r " + hostName + " has left the chat.");
                            sendMessageToClient.interrupt();
                            return;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        done = true;
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
        clientSocket.close();
        serverSocket.close();
        sc.close();
    }
}
