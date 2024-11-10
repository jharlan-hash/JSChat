import java.net.*;
import java.io.*;
import java.util.Scanner;

public class JarDrop{
    public static void main (String[] args) throws IOException, InterruptedException {
        if (args.length == 0){
            System.out.println("Acting as a client");
            client();
        } else if (args.length == 1){
            System.out.println("Acting as a server");
            server(args);
        } else {
            System.out.println("too many arguments supplied");
        }
    }

    public static void client () throws IOException, InterruptedException{
        Socket socket = new Socket();
        Scanner sc = new Scanner(System.in);
        socket.connect(new InetSocketAddress("127.0.0.1", 5001), 1000);
        System.out.println("Connection successful!");

        DataInputStream dataIn = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        Thread getMessageFromServer = new Thread(){
            public void run() {
                while (true) {
                    try {
                        String serverMessage = dataIn.readUTF();
                        System.out.println(serverMessage);

                        if (serverMessage.equals("Exit")){
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread sendMessageToServer = new Thread(){
            public void run() {
                while (true) {
                    try {
                        String clientMessage = sc.nextLine();
                        dataOut.writeUTF("[client] " + clientMessage);

                        if (clientMessage.equals("Exit")){
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        getMessageFromServer.start();
        sendMessageToServer.start();
        getMessageFromServer.join();
        sendMessageToServer.join();

        dataIn.close();
        dataOut.close();
        socket.close();
        sc.close();
    }

    public static void server (String[] args) throws IOException, InterruptedException{
        ServerSocket serverSocket = new ServerSocket(5001);
        Scanner sc = new Scanner(System.in);
        System.out.println("Listening for clients...");

        Socket clientSocket = serverSocket.accept();
        String clientSocketIP = clientSocket.getInetAddress().toString();
        int clientSocketPort = clientSocket.getPort();
        System.out.println("[IP: " + clientSocketIP + " ,Port: " + clientSocketPort +"]  " + "Client Connection Successful!");

        DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());

        Thread getMessageFromClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        String clientMessage = dataIn.readUTF();
                        System.out.println(clientMessage);

                        if (clientMessage.equals("Exit")){
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread sendMessageToClient = new Thread(){
            public void run() {
                while (true) {
                    try {
                        System.out.print("[server]");
                        String serverMessage = sc.nextLine();
                        dataOut.writeUTF("[server] " + serverMessage);

                        if (serverMessage.equals("Exit")){
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        getMessageFromClient.start();
        sendMessageToClient.start();

        getMessageFromClient.join();
        sendMessageToClient.join();

        dataIn.close();
        dataOut.close();
        clientSocket.close();
        serverSocket.close();
        sc.close();
    }
}
