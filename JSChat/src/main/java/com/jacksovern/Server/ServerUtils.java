package com.jacksovern.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerUtils {
    // why are these here
    public static final String USER_PROMPT = "[you] ";
    public static final String EXIT_MESSAGE = "/exit";
    public static final String NICK_MESSAGE = "/nick";

    public static final int portNumber = 1000;

    public static boolean serverIsRunning = true;
    public static boolean isFirstClient = true;

    public static byte[] readKeyBytes(DataInputStream dataIn, int length) throws IOException {
        byte[] keyBytes = new byte[length]; // public key is usually 422 bytes long

        int p = 0;
        while (p < keyBytes.length) {
            int read = dataIn.read(keyBytes);
            if (read == -1) {
                throw new RuntimeException("Premature end of stream in ServerUtils"); 
            }
            p += read;
        }

        return keyBytes;
    }

    // this is a ridiculous way to do this
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
