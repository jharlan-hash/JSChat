import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void client_mode (String ip, int port) throws IOException, InterruptedException {
        Socket socket = new Socket();
        Scanner sc = new Scanner(System.in);

        socket.connect(new InetSocketAddress(ip, port), 1000);
        System.out.println("Connection successful!");

        DataInputStream dataIn = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        InetAddress addr = InetAddress.getByName(ip);
        String hostName = addr.getHostName();


        Thread sendMessageToServer = new Thread(){
            public void run() {
                boolean done = false;

                while (!done) {
                    try {
                        if (!Thread.interrupted()){
                            String messageSent = sc.nextLine();
                            dataOut.writeUTF(messageSent);
                            System.out.print("[you] ");
                        } else {
                            System.out.println("sendMessageToServer interrupted");
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

        Thread getMessageFromServer = new Thread(){
            public void run() {
                boolean done = false;
                String messageReceived = "";

                while (!done) {
                    try {

                        if (!messageReceived.contains("/exit")){
                            System.out.print("[you] ");
                            messageReceived = dataIn.readUTF();
                            System.out.println("\r[" + hostName + "] " + messageReceived);
                        } else {
                            System.out.print("\r " + hostName + " has left the chat.");
                            sendMessageToServer.interrupt();
                            return;
                        }
                    } catch (IOException e) {
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
