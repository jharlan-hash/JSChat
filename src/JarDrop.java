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
            server(ip, port);
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


        Thread sendMessageToServer = new Thread(){

            public void run() {
                boolean done = false;

                while (!done) {
                    try {
                        if (!Thread.interrupted()){
                            String messageSent = sc.nextLine();
                            dataOut.writeUTF(messageSent);
                            System.out.print("[you] ");
                        } else {
                            System.out.println("sendMessageToServer interrupted");
                            done = true;
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread getMessageFromServer = new Thread(){
            public void run() {
                boolean done = false;
                String messageReceived = "";

                while (!done) {
                    try {

                        if (!messageReceived.contains("/exit")){
                            System.out.print("[you] ");
                            messageReceived = dataIn.readUTF();
                            System.out.println("\r[" + hostName + "] " + messageReceived);
                        } else {
                            System.out.print("\r " + hostName + " has left the chat.");
                            sendMessageToServer.interrupt();
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

    public static void server (String ip, int port) throws IOException, InterruptedException{
        ServerSocket serverSocket = new ServerSocket(port);
        Scanner sc = new Scanner(System.in);
        System.out.println("Listening for clients...");

        Socket clientSocket = serverSocket.accept();

        DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());

        InetAddress addr = InetAddress.getByName(ip);
        String hostName = addr.getHostName();


        Thread sendMessageToClient = new Thread(){
            public void run() {
                boolean done = false;

                while (!done) {
                    try {
                        if (!Thread.interrupted()){
                            System.out.print("[you] ");
                            String messageSent = sc.nextLine();
                            dataOut.writeUTF(messageSent);
                        } else {
                            System.out.println("sendMessageToClient interrupted");
                            done = true;
                            return;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        Thread getMessageFromClient = new Thread(){
            public void run() {
                boolean done = false;
                String messageReceived = "";

                while (!done) {
                    try {
                        if (!messageReceived.contains("/exit")){
                            messageReceived = dataIn.readUTF();
                            System.out.println("\r[" + hostName + "] " + messageReceived);
                            System.out.print("[you] ");
                        } else {
                            System.out.println("\r " + hostName + " has left the chat.");
                            sendMessageToClient.interrupt();
                            return;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        done = true;
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
