/* ChatUtils.java */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.Scanner;

public class ChatUtils {
    public static final String USER_PROMPT = "[you] ";
    public static final String EXIT_MESSAGE = "/exit";
    public static final String NICK_MESSAGE = "/nick";

    public static boolean serverIsRunning = true;

    public static void main (String[] args) throws Exception {
        String operationMode, ipAddress;
        int portNumber;

        if (args.length != 3) {
            System.out.println("Usage: ./build.sh <operationMode> <ipAddress> <portNumber>");
            System.exit(1);
        }

        operationMode = args[0];
        ipAddress = args[1].equals("self") ? getLocalIPAddress() : args[1];

        try {
            portNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid port number.");
            portNumber = -1;
            System.exit(1);
        }

        if (args[1].equals("self")) {
            System.out.println("IP Address: " + ipAddress);
        }

        try {
            switch (operationMode) {
                case "con":
                    Client.clientMode(ipAddress, portNumber);
                    break;
                case "srv":
                    Server.serverMode(portNumber);
                    break;
                default:
                    System.out.println("Please enter a valid operationMode (con or srv)");
                    break;
                
            }
        } catch (IOException e) {
            System.out.println("Connection failed - make sure the IP and portNumber are correct.");
        }
    }

    public static String promptUserInput(DataOutputStream dataOut, Scanner scanner) throws IOException {
        System.out.print(USER_PROMPT);
        String messageToSend = scanner.nextLine();

        return messageToSend;
    }

    public static byte[] receiveEncryptedMessage(DataInputStream dataIn) throws IOException {
        byte[] encryptedMessage = new byte[384];

        try {
            dataIn.read(encryptedMessage);
        } catch (IOException e) {
            System.out.println("Connection closed by server.");
            return null;
        }         

        return encryptedMessage;
    }

    public static byte[] readPublicKeyBytes(DataInputStream dataIn) throws IOException {
        byte[] keyBytes = new byte[422];

        int p = 0;
        while (p < keyBytes.length) {
            int read = dataIn.read(keyBytes);
            if (read == -1) {
                throw new RuntimeException("Premature end of stream");
            }
            p += read;
        }

        return keyBytes;
    }

    public static String handleUserCommands(String message, DataOutputStream dataOut, String currentNickname, PublicKey publicKey) throws Exception {
        if (message.startsWith(ChatUtils.NICK_MESSAGE)) {
            String nickname = extractNickname(message);
            dataOut.write(RSA.encrypt("\r{Server} " + currentNickname + " changed their nickname to " + nickname, publicKey));
            return nickname;
        }

        return currentNickname;
    }

    public static String extractNickname (String message){
        return message.split(" ")[1];
    }

    private static String getLocalIPAddress() {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 0);
            return socket.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
