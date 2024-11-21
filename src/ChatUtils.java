/* ChatUtils.java */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ChatUtils {
    public static final String USER_PROMPT = "[you] ";
    public static final String EXIT_MESSAGE = "/exit";
    public static final String NICK_MESSAGE = "/nick";

    public static void main (String[] args) throws Exception {
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
            try {
                Client.clientMode(ip, port);
            } catch (IOException e) {
                System.out.println("Connection failed - make sure the IP and port are correct.");
            }
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
        String messageToSend = sc.nextLine();

        return messageToSend;
    }

    public static byte[] getMessage(DataInputStream dataIn) throws IOException {
        byte[] encryptedMessage = new byte[384];

        try {
            dataIn.read(encryptedMessage);
        } catch (IOException e) {
            System.out.println("Connection closed by server.");
            return null;
        }         

        return encryptedMessage;
    }

    public static byte[] readPublicKey(DataInputStream dataIn) throws IOException {
        byte[] publicKeyBytes = new byte[422];

        for (int p = 0; p < publicKeyBytes.length; ) {
            int read = dataIn.read(publicKeyBytes);
            if (read == -1) {
                throw new RuntimeException("Premature end of stream");
            }
            p += read;
        }

        return publicKeyBytes;
    }

    public static String parseCommands(String message, DataOutputStream dataOut ,String hostname) throws IOException {
        if (message.startsWith(ChatUtils.NICK_MESSAGE)) {
            String nickname = nickname(hostname, message);
            dataOut.writeUTF("\r{Server} " + hostname + " changed their nickname to " + nickname);
            return nickname;
        }

        return hostname;
    }

    public static String nickname (String hostname, String message){
        String[] messageArray = message.split(" ");
        hostname = messageArray[1];

        return hostname;
    }

    private static String getLocalIP() {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 0);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
