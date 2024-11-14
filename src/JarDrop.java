/* JarDrop.java */

import java.io.IOException;
import java.util.Scanner;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.InetAddress;

public class JarDrop {
    public static final String USER_PROMPT = "[you] ";
    public static final String EXIT_MESSAGE = "/exit";

    public static void main (String[] args) throws IOException, InterruptedException {
        String mode = "";
        String ip = "";
        int port = -1;

        if (args.length == 3){
            mode = args[0];
            ip = args[1];
            try {
                port = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                System.out.println("Please enter a valid port number.");
                System.exit(1);
            }
        } else {
            System.out.println("Usage: java JarDrop <mode> <ip> <port>");
            System.exit(1);
        }

        if (mode.equals("con")){
            System.out.println("Acting as a client");
            Client.clientMode(ip, port);
        } else if (mode.equals("srv")){
            System.out.println("Acting as a server");
            Server.serverMode(ip, port);
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

    public static String getMessage(DataInputStream dataIn, String ip) throws IOException {
        InetAddress addr = InetAddress.getByName(ip);
        String hostName = addr.getHostName();
        String messageReceived;

        try {
            messageReceived = dataIn.readUTF();
        } catch (IOException e) {
            System.out.println("Connection closed by server.");
            shutdown(dataIn);
            return "";
        }         

        // messageReceived = "\r[" + hostName + "] " + messageReceived;

        if (messageReceived.equals(EXIT_MESSAGE)){
            shutdown();
        }

        return messageReceived;
    }

    public static void shutdown(DataInputStream dataIn, DataOutputStream dataOut, Scanner sc) throws IOException { dataIn.close(); dataOut.close(); sc.close(); System.exit(0); }
    public static void shutdown(DataInputStream dataIn, DataInputStream dataInTwo) throws IOException { dataIn.close(); dataInTwo.close(); System.exit(0); }
    public static void shutdown(DataInputStream dataIn, DataOutputStream dataOut) throws IOException { dataIn.close(); dataOut.close(); System.exit(0); }
    public static void shutdown(DataOutputStream dataOut, Scanner sc) throws IOException { dataOut.close(); sc.close(); System.exit(0); }
    public static void shutdown(DataInputStream dataIn) throws IOException { dataIn.close(); System.exit(0); }
    public static void shutdown(DataOutputStream dataOut) throws IOException { dataOut.close(); System.exit(0); }
    public static void shutdown(Scanner sc) { sc.close(); System.exit(0); }
    public static void shutdown() { System.exit(0); }

}
