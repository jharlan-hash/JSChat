import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
        messageToSend = messageToSend.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", ""); // remove control characters

        return messageToSend;
    }

    public static byte[] receiveEncryptedMessage(DataInputStream dataIn) throws IOException {
        byte[] encryptedMessage = new byte[384]; // 384 bytes is always the size of the encrypted message

        try {
            dataIn.read(encryptedMessage);
        } catch (IOException e) {
            System.out.println("Connection closed by server.");
            return null;
        }         

        return encryptedMessage;
    }

    public static byte[] readPublicKeyBytes(DataInputStream dataIn) throws IOException {
        byte[] keyBytes = new byte[422]; // 422 is always the size of the public key

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

    public static void shutdown(
        DataInputStream dataIn1, 
        DataOutputStream dataOut1, 
        DataInputStream dataIn2, 
        DataOutputStream dataOut2, 
        Scanner scanner, 
        Socket clientSocket1, 
        Socket clientSocket2, 
        ServerSocket serverSocket
    ) throws IOException {
        System.out.println("Shutting down...");
        closeQuietly(dataIn1);
        closeQuietly(dataOut1);
        closeQuietly(dataIn2);
        closeQuietly(dataOut2);
        closeQuietly(scanner);
        closeQuietly(clientSocket1);
        closeQuietly(clientSocket2);
        closeQuietly(serverSocket);
    }

    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }
    }
}
