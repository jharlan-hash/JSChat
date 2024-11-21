/* Server.java */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HexFormat;
import java.util.Scanner;

public class Server {
    public static boolean isRunning = true;
    public static void serverMode (int port) throws IOException, InterruptedException, Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        Scanner sc = new Scanner(System.in);

        System.out.println("Listening for clients...");
        Socket firstClientSocket = serverSocket.accept(); 
        System.out.println("Client connected");
        DataInputStream firstDataIn = new DataInputStream(firstClientSocket.getInputStream());
        DataOutputStream firstDataOut = new DataOutputStream(firstClientSocket.getOutputStream());

        byte[] firstPublicKeyBytes = new byte[422];

        for (int p = 0; p < firstPublicKeyBytes.length; ) {
            int read = firstDataIn.read(firstPublicKeyBytes);
            if (read == -1) {
                throw new RuntimeException("Premature end of stream");
            }
            p += read;
        }

        System.out.println("First public key read");

        Socket secondClientSocket = serverSocket.accept(); 
        System.out.println("Second client connected");
        firstDataOut.writeUTF("\rSecond user connected");
        DataInputStream secondDataIn = new DataInputStream(secondClientSocket.getInputStream());
        DataOutputStream secondDataOut = new DataOutputStream(secondClientSocket.getOutputStream());

        byte[] secondPublicKeyBytes = new byte[422];

        for (int p = 0; p < secondPublicKeyBytes.length; ) {
            int read = secondDataIn.read(secondPublicKeyBytes);
            if (read == -1) {
                throw new RuntimeException("Premature end of stream");
            }
            p += read;
        }
        System.out.println("Second public key read");

        secondDataOut.write(firstPublicKeyBytes); // send first client's public key to second client
        System.out.println("First public key sent to second client");
        firstDataOut.write(secondPublicKeyBytes); // send second client's public key to first client
        System.out.println("Second public key sent to first client");

        Thread getMessageFromFirstClient = createThread(firstClientSocket, firstDataIn, secondDataOut);
        Thread getMessageFromSecondClient = createThread(secondClientSocket, secondDataIn, firstDataOut);

        getMessageFromFirstClient.start();
        getMessageFromSecondClient.start();

        getMessageFromFirstClient.join();
        getMessageFromSecondClient.join();

        shutdown(firstDataIn, firstDataOut, secondDataIn, secondDataOut, sc, firstClientSocket, secondClientSocket, serverSocket);
    }



    private static Thread createThread(Socket clientSocket, DataInputStream dataIn, DataOutputStream dataOut) {
        Thread getMessageFromClient = new Thread(){
            public void run() {
                while (isRunning) {
                    try {
                        System.out.println("Reading message from client...");
                        byte[] message = ChatUtils.getMessage(dataIn);
                        System.out.println("Message read: " + HexFormat.of().formatHex(message));
                        dataOut.write(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;

                    }
                }
            }
        };

        return getMessageFromClient;
    }

    private static void shutdown(

        DataInputStream firstDataIn, 
        DataOutputStream firstDataOut, 
        DataInputStream secondDataIn, 
        DataOutputStream secondDataOut, 
        Scanner sc, 
        Socket firstClientSocket, 
        Socket secondClientSocket, 
        ServerSocket serverSocket
    ) throws IOException {
        System.out.println("Shutting down server...");
        closeQuietly(firstDataIn);
        closeQuietly(firstDataOut);
        closeQuietly(secondDataIn);
        closeQuietly(secondDataOut);
        closeQuietly(sc);
        closeQuietly(firstClientSocket);
        closeQuietly(secondClientSocket);
        closeQuietly(serverSocket);
        System.exit(0);
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }
    }
}
