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
        Scanner scanner = new Scanner(System.in);

        System.out.println("Listening for clients...");
        Socket clientSocket1 = serverSocket.accept(); 
        System.out.println("Client connected");
        DataInputStream dataIn1 = new DataInputStream(clientSocket1.getInputStream());
        DataOutputStream dataOut1 = new DataOutputStream(clientSocket1.getOutputStream());

        byte[] firstPublicKeyBytes = ChatUtils.readPublicKeyBytes(dataIn1);
        System.out.println("First public key read");

        Socket clientSocket2 = serverSocket.accept(); 
        System.out.println("Second client connected");
        dataOut1.writeUTF("\rSecond user connected");
        DataInputStream dataIn2 = new DataInputStream(clientSocket2.getInputStream());
        DataOutputStream dataOut2 = new DataOutputStream(clientSocket2.getOutputStream());

        byte[] secondPublicKeyBytes = ChatUtils.readPublicKeyBytes(dataIn2);
        System.out.println("Second public key read");

        dataOut2.write(firstPublicKeyBytes);
        System.out.println("First public key sent to second client");
        dataOut1.write(secondPublicKeyBytes);
        System.out.println("Second public key sent to first client");

        Thread thread1 = createThread(clientSocket1, dataIn1, dataOut2);
        Thread thread2 = createThread(clientSocket2, dataIn2, dataOut1);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        ChatUtils.shutdown(dataIn1, dataOut1, dataIn2, dataOut2, scanner, clientSocket1, clientSocket2, serverSocket);
    }



    private static Thread createThread(Socket clientSocket, DataInputStream dataIn, DataOutputStream dataOut) {
        Thread clientThread = new Thread(){
            public void run(){
                while (ChatUtils.serverIsRunning) {
                    try {
                        byte[] message = ChatUtils.receiveEncryptedMessage(dataIn);
                        dataOut.write(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;

                    }
                }
            }
        };

        return clientThread;
    }

}
