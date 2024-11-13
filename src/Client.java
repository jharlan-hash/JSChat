import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void clientMode (String ip, int port) throws IOException, InterruptedException {
        Socket socket = new Socket();
        Scanner sc = new Scanner(System.in);

        socket.connect(new InetSocketAddress(ip, port), 1000); // connect to server with 1000ms timeout
        System.out.println("Connection successful!"); 

        DataInputStream dataIn = new DataInputStream(socket.getInputStream()); 
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream()); 

        InetAddress addr = InetAddress.getByName(ip); 
        String hostName = addr.getHostName(); 


        Thread sendMessageToServer = new Thread(){ // create a new thread for sending messages to the server
            public void run() {
                while (true) {
                    try {
                        System.out.print("[you] "); 
                        String messageSent = sc.nextLine();
                        dataOut.writeUTF(messageSent); 

                        if (messageSent.contains("/exit")){ // if the USER types /exit, exit the program
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
                while (true) {
                    try {
                        String messageReceived = dataIn.readUTF();
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
