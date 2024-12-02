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

        byte[] firstPublicKeyBytes = ChatUtils.readKeyBytes(dataIn1, 422);
        System.out.println("First public key read");

        Socket clientSocket2 = serverSocket.accept(); 
        System.out.println("Second client connected");
        dataOut1.writeUTF("\rSecond user connected");
        DataInputStream dataIn2 = new DataInputStream(clientSocket2.getInputStream());
        DataOutputStream dataOut2 = new DataOutputStream(clientSocket2.getOutputStream());

        byte[] secondPublicKeyBytes = ChatUtils.readKeyBytes(dataIn2, 422);
        System.out.println("Second public key read");

        dataOut2.write(firstPublicKeyBytes);
        dataOut1.write(secondPublicKeyBytes);

        byte[] AESKeyBytes = new byte[384];
        if (dataIn1.read(AESKeyBytes) < 384){
            System.out.println("AES key not received in full.");
        }

        dataOut1.write(AESKeyBytes);
        dataOut2.write(AESKeyBytes); 
        dataOut1.flush();
        dataOut2.flush();

        dataIn2.readNBytes(384); // discarding unused AES key

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
                        int messageLength = dataIn.readInt();
                        if (messageLength <= 0) {
                            continue;
                        }
                        
                        byte[] message = new byte[messageLength];
                        int bytesRead = 0;
                        while (bytesRead < messageLength) {
                            int result = dataIn.read(message, bytesRead, messageLength - bytesRead);
                            if (result == -1) {
                                break;
                            }
                            bytesRead += result;
                        }
                        
                        dataOut.writeInt(messageLength);
                        dataOut.write(message, 0, messageLength);
                        dataOut.flush();
                    } catch (IOException e) {
                        System.out.println("Client disconnected");
                        ChatUtils.serverIsRunning = false;
                        return;

                    }
                }
            }
        };

        return clientThread;
    }

}
