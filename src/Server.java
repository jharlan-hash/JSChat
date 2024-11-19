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
            String hostname = clientSocket.getInetAddress().getHostName();
            public void run() {
                while (true) {
                    try {
                        String message = ChatUtils.getMessage(dataIn);

                        //TODO make command checking a separate function
                        if (message == null || message.equals(ChatUtils.EXIT_MESSAGE)){
                            dataOut.writeUTF("\r{Server} " + hostname + " has left the chat - use /exit to leave");
                            return;
                        } else if (message.startsWith(ChatUtils.NICK_MESSAGE)) {
                            String[] messageArray = message.split(" ");
                            hostname = messageArray[1];
                        }

                        message = "\r[" + hostname + "] " + message;

                        if (!(message.startsWith("\r[" + hostname + "] /"))) { // checking if the message is a command
                            dataOut.writeUTF(message);
                        }
                        System.out.println(message);
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
