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

        InetAddress addr = InetAddress.getByName(ip);
        String hostName = addr.getHostName();


        Thread sendMessageToClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        System.out.print("[you] ");
                        String messageSent = sc.nextLine();
                        dataOut.writeUTF(messageSent);

                        if (messageSent.contains("/exit")){
                            System.exit(0);
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
                while (true) {
                    try {
                        System.out.print("[you] ");
                        String messageReceived = dataIn.readUTF();
                        System.out.println("\r[" + hostName + "] " + messageReceived);

                        if (messageReceived.contains("/exit")){
                            System.exit(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    // TODO - make this into a method called multiple times
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
