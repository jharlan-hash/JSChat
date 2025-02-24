package com.jacksovern.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ChatUtils {
    public static final String USER_PROMPT = "[you] ";
    public static final String EXIT_MESSAGE = "/exit";
    public static final String NICK_MESSAGE = "/nick";

    public static boolean isFirstClient = true;
    public static int portNumber = 1000;

    public static boolean getMessageFromServer(Scanner scanner, DataInputStream dataIn, SecretKey AESKey) throws IOException {
        byte[] encryptedMessage;

        try{
            encryptedMessage = ChatUtils.receiveEncryptedMessage(dataIn);
        } catch (Exception e) {
            scanner.close();
            dataIn.close();
            System.out.println("Server disconnected");
            return false;
        }

        String message;
        try {
            message = AES.decrypt(encryptedMessage, AESKey);
        } catch (Exception ignored) {
            System.out.println("error decrypting message");
            scanner.close();
            dataIn.close();
            return false; 
        }

        System.out.println(message);
        System.out.print(ChatUtils.USER_PROMPT);
        return true;
    }

    public static String sendMessageToServer(Scanner scanner, DataOutputStream dataOut, SecretKey AESKey, String hostname) throws
    IOException, 
    IllegalBlockSizeException, 
    BadPaddingException, 
    InvalidKeyException, 
    InvalidAlgorithmParameterException, 
    NoSuchAlgorithmException, 
    NoSuchPaddingException {
        String message = ChatUtils.promptUserInput(dataOut, scanner);

        if (message.startsWith(ChatUtils.NICK_MESSAGE)){
            hostname = ChatUtils.changeNickname(message, dataOut, hostname, AESKey);
        } else if (message.equals(ChatUtils.EXIT_MESSAGE)){
            byte[] exitNotification = AES.encrypt("\r{Server} " + hostname + " has left the chat - use /exit to leave", AESKey);
            dataOut.writeInt(exitNotification.length);
            dataOut.write(exitNotification);
            dataOut.close();
            return null;
        }

        if (!(message.startsWith("/"))) { // checking if the message is a command
            message = "\r[" + hostname + "] " + message;
            byte[] encryptedMessage = AES.encrypt(message, AESKey);

            dataOut.writeInt(encryptedMessage.length);
            dataOut.write(encryptedMessage);
            dataOut.flush();
        } else if (!(message.equals(ChatUtils.EXIT_MESSAGE) | message.startsWith(ChatUtils.NICK_MESSAGE))) {
            System.out.println("Invalid command.");
            System.out.println("Commands: /exit, /nick <newNickname>");
        }

        return hostname;
    }

    public static String promptUserInput(DataOutputStream dataOut, Scanner scanner) throws IOException {
        System.out.print(USER_PROMPT);
        String messageToSend = scanner.nextLine();

        messageToSend = messageToSend.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", ""); // remove control characters

        return messageToSend;
    }

    public static byte[] receiveEncryptedMessage(DataInputStream dataIn) throws IOException {
        int length = dataIn.readInt();
        if (length <= 0) {
            throw new IOException("Invalid message length: " + length);
        }

        byte[] encryptedMessage = new byte[length];

        int bytesRead = 0;

        while (bytesRead < length) {
            int result = dataIn.read(encryptedMessage, bytesRead, length - bytesRead);
            if (result == -1) {
                throw new IOException("End of stream reached before message was complete");
            }
            bytesRead += result;
        }

        return encryptedMessage;
    }

    public static byte[] readKeyBytes(DataInputStream dataIn, int length) throws IOException {
        byte[] keyBytes = new byte[length]; // public key is usually 422 bytes long

        int p = 0;
        while (p < keyBytes.length) {
            int read = dataIn.read(keyBytes);
            if (read == -1) {
                throw new RuntimeException("erroneous key value in ChatUtils");
            }
            p += read;
        }

        if (p != 422) {
            System.out.println("Erroneous key value received: " + p);
        }
        return keyBytes;
    }

    public static String changeNickname(String message, DataOutputStream dataOut, String currentNickname, SecretKey AESKey) throws 
    IOException, 
    BadPaddingException, 
    IllegalBlockSizeException, 
    InvalidAlgorithmParameterException, 
    InvalidKeyException, 
    NoSuchAlgorithmException, 
    NoSuchPaddingException {
        String[] nicknameArray = message.split(" ");
        String nickname;

        if (nicknameArray.length != 2) {
            System.out.println("Invalid /nick usage.");
            System.out.println("Usage: /nick <newNickname>");
            return currentNickname;
        } else {
            nickname = nicknameArray[1];
        }

        byte[] nicknameNotification = AES.encrypt("\r{Server} " + currentNickname + " changed their nickname to " + nickname, AESKey);
        dataOut.writeInt(nicknameNotification.length);
        dataOut.write(nicknameNotification);
        return nickname;
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
        closeQuietly(clientSocket1);
        closeQuietly(clientSocket2);
        closeQuietly(dataIn1);
        closeQuietly(dataIn2);
        closeQuietly(dataOut1);
        closeQuietly(dataOut2);
        closeQuietly(scanner);
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
