/* Server.java */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public static void serverMode (int port) throws IOException, InterruptedException, Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        Scanner sc = new Scanner(System.in);

        System.out.println("Listening for clients...");
        Socket firstClientSocket = serverSocket.accept(); 
        System.out.println("Client connected");
        DataInputStream firstDataIn = new DataInputStream(firstClientSocket.getInputStream());
        DataOutputStream firstDataOut = new DataOutputStream(firstClientSocket.getOutputStream());

        byte[] firstPublicKeyBytes = ChatUtils.readPublicKey(firstDataIn);
        System.out.println("First public key read");

        Socket secondClientSocket = serverSocket.accept(); 
        System.out.println("Second client connected");
        firstDataOut.writeUTF("\rSecond user connected");
        DataInputStream secondDataIn = new DataInputStream(secondClientSocket.getInputStream());
        DataOutputStream secondDataOut = new DataOutputStream(secondClientSocket.getOutputStream());

        byte[] secondPublicKeyBytes = ChatUtils.readPublicKey(secondDataIn);
        System.out.println("Second public key read");

        secondDataOut.write(firstPublicKeyBytes);
        System.out.println("First public key sent to second client");
        firstDataOut.write(secondPublicKeyBytes);
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
            public void run(){
                while (ChatUtils.serverIsRunning) {
                    try {
                        byte[] message = ChatUtils.receiveMessage(dataIn);
                        dataOut.write(message);
                    } catch (IOException e) {
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
