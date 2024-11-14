/* chatUtils.java */

import java.io.IOException;
import java.util.Scanner;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class chatUtils {
    public static final String USER_PROMPT = "[you] ";
    public static final String EXIT_MESSAGE = "/exit";
    public static final String NULL_STRING = "";

    public static void main (String[] args) throws IOException, InterruptedException {
        String mode = NULL_STRING;
        String ip = NULL_STRING;
        int port = -1;

        if (args.length == 3){
            mode = args[0];
            if (args[1].equals("self")) {
                try(final DatagramSocket socket = new DatagramSocket()){
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                    ip = socket.getLocalAddress().getHostAddress();
                }
                System.out.println("IP Address: " + ip);
            } else {
                ip = args[1];
            }

            try {
                port = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                System.out.println("Please enter a valid port number.");
                System.exit(1);
            }
        } else {
            System.out.println("Usage: ./build.sh <mode> <ip> <port>");
            System.exit(1);
        }

        if (mode.equals("con")){
            Client.clientMode(ip, port);
        } else if (mode.equals("srv")){
            Server.serverMode(port);
        } else {
            System.out.println("Please enter a valid mode (con or srv)");
        }
    }

    public static void sendMessage(DataOutputStream dataOut, Scanner sc) throws IOException {
        System.out.print(USER_PROMPT);
        String messageSent = sc.nextLine();

        if (messageSent.equals(EXIT_MESSAGE)){
            shutdown(dataOut, sc);
        }

        dataOut.writeUTF(messageSent);

        return;
    }

    public static String getMessage(DataInputStream dataIn) throws IOException {
        String messageReceived;

        try {
            messageReceived = dataIn.readUTF();
        } catch (IOException e) {
            System.out.println("Connection closed by server.");
            shutdown(dataIn);
            return NULL_STRING;
        }         

        // messageReceived = "\r[" + hostName + "] " + messageReceived;

        if (messageReceived.equals(EXIT_MESSAGE)){
            shutdown();
        }

        return messageReceived;
    }

    public static void shutdown(DataOutputStream dataOut, Scanner sc) throws IOException { dataOut.close(); sc.close(); System.exit(0); }
    public static void shutdown(DataInputStream dataIn) throws IOException { dataIn.close(); System.exit(0); }
    public static void shutdown() { System.exit(0); }

}
