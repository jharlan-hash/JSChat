import java.net.*;
import java.io.*;
import java.util.Scanner;

public class JarDrop{
    public static void main (String[] args) throws IOException, InterruptedException {
        // args[0] is command name
        // args[1] is ip
        // args[2] is port (optional, defaults to 22(?))
        String commandName = "";
        String ip = "";
        int port = 0;

        if (args.length == 3){
            commandName = args[0];
            ip = args[1];
            port = Integer.parseInt(args[2]);
        } else {
            System.out.println("Please enter three arguments - command name (con or srv), ip, and port");
            System.exit(1);
        }

        if (commandName.contains("con")){
            System.out.println("Acting as a client");
            client(ip, port);
        } else if (commandName.contains("serv") || commandName.contains("srv")){
            System.out.println("Acting as a server");
            server(port);
        } else {
            System.out.println("too many arguments supplied");
        }
    }

    public static void client (String ip, int port) throws IOException, InterruptedException{
        Socket socket = new Socket();
        Scanner sc = new Scanner(System.in);

        socket.connect(new InetSocketAddress(ip, port), 1000);
        System.out.println("Connection successful!");

        DataInputStream dataIn = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        InetAddress addr = InetAddress.getByName(ip);
        String hostName = addr.getHostName();

        Thread getMessageFromServer = new Thread(){
            public void run() {
                boolean done = false;

                while (!done) {
                    try {
                        String serverMessage = dataIn.readUTF();
                        System.out.println("\r[" + hostName + "] " + serverMessage);
                        System.out.print("[you]");

                        if (serverMessage.contains("/exit")){
                            done = true;
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
                boolean done = false;

                while (!done) {
                    try {
                        String clientMessage = sc.nextLine();
                        dataOut.writeUTF(clientMessage);

                        if (clientMessage.contains("/exit")){
                            getMessageFromServer.interrupt();
                            done = true;
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

    public static void server (int port) throws IOException, InterruptedException{
        ServerSocket serverSocket = new ServerSocket(port);
        Scanner sc = new Scanner(System.in);
        System.out.println("Listening for clients...");

        Socket clientSocket = serverSocket.accept();

        DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());

        InetAddress addr = InetAddress.getByName(ip);
        String hostName = addr.getHostName();

        Thread getMessageFromClient = new Thread(){
            public void run() {
                boolean done = false;

                while (!done) {
                    try {
                        String clientMessage = dataIn.readUTF();
                        System.out.println("\r[" + hostName + "] " + clientMessage);
                        System.out.print("[client]");

                        if (clientMessage.contains("/exit")){
                            done = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        done = true;
                        return;
                    }
                }
            }
        };

        Thread sendMessageToClient = new Thread(){
            public void run() {
                boolean done = false;

                while (!done) {
                    try {
                        System.out.print("[server] ");
                        String serverMessage = sc.nextLine();
                        dataOut.writeUTF(serverMessage);

                        if (serverMessage.contains("/exit")){
                            getMessageFromClient.interrupt();
                            done = true;
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
