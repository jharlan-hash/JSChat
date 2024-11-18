/* Server.java */
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Server {
    public static void serverMode (int port) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(port);
        Scanner sc = new Scanner(System.in);

        System.out.println("Listening for clients...");
        Socket firstClientSocket = serverSocket.accept(); 
        DataInputStream firstDataIn = new DataInputStream(firstClientSocket.getInputStream());
        DataOutputStream firstDataOut = new DataOutputStream(firstClientSocket.getOutputStream());
        System.out.println("Client connected");


        Socket secondClientSocket = serverSocket.accept(); 
        System.out.println("Second client connected");
        firstDataOut.writeUTF("\rSecond user connected");
        DataInputStream secondDataIn = new DataInputStream(secondClientSocket.getInputStream());
        DataOutputStream secondDataOut = new DataOutputStream(secondClientSocket.getOutputStream());

        String firstClientHostName = firstClientSocket.getInetAddress().getHostName();
        String secondClientHostName = secondClientSocket.getInetAddress().getHostName();

        Thread getMessageFromFirstClient = createThread(firstClientSocket, firstDataIn, secondDataOut, firstClientHostName);
        Thread getMessageFromSecondClient = createThread(secondClientSocket, secondDataIn, firstDataOut, secondClientHostName);

        getMessageFromFirstClient.start();
        getMessageFromSecondClient.start();

        getMessageFromFirstClient.join();
        getMessageFromSecondClient.join();

        shutdown(firstDataIn, firstDataOut, secondDataIn, secondDataOut, sc, firstClientSocket, secondClientSocket, serverSocket);
    }

    public static Thread createThread(Socket clientSocket, DataInputStream dataIn, DataOutputStream dataOut, String hostname) {
        Thread getMessageFromClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        String message = ChatUtils.getMessage(dataIn);
                        
                        if (message == null || message.equals(ChatUtils.EXIT_MESSAGE)){
                            dataOut.writeUTF("\r{Server} " + hostname + " has left the chat - use /exit to leave");
                            break;
                        } else if (message.startsWith(ChatUtils.NICK_MESSAGE)) {
                            String[] messageArray = message.split(" ");
                            for (int i = 0; i < messageArray.length; i++) {
                                System.out.println("\nmesssageArray[" + i + "]: " + messageArray[i] + " ");
                            }
                        }

                        message = "\r[" + hostname + "] " + message;
                        System.out.println(message);
                        dataOut.writeUTF(message);
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
