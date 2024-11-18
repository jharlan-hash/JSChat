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

    public static void main (String[] args) throws IOException, InterruptedException {
        String mode;
        String ip;
        int port;

        if (args.length != 3) {
            System.out.println("Usage: ./build.sh <mode> <ip> <port>");
            System.exit(1);
        }

        mode = args[0];
        ip = args[1].equals("self") ? getLocalIP() : args[1];

        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid port number.");
            port = -1;
            System.exit(1);
        }

        if (args[1].equals("self")) {
            System.out.println("IP Address: " + ip);
        }

        switch (mode) {
            case "con":
                Client.clientMode(ip, port);
                break;
            case "srv":
                Server.serverMode(port);
                break;
            default:
                System.out.println("Please enter a valid mode (con or srv)");
                break;
        }
    }

    public static String sendMessage(DataOutputStream dataOut, Scanner sc) throws IOException {
        System.out.print(USER_PROMPT);
        String messageSent = sc.nextLine();
        dataOut.writeUTF(messageSent);

        return messageSent;
    }

    public static String getMessage(DataInputStream dataIn) throws IOException {
        String messageReceived;

        try {
            messageReceived = dataIn.readUTF();
        } catch (IOException e) {
            System.out.println("Connection closed by server.");
            return null;
        }         

        return messageReceived;
    }

    public static String getLocalIP() {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 0);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
