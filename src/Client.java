/* Client.java */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

public class Client {
    public static void clientMode (String ip, int port) throws Exception {
        Socket socket = new Socket();
        Scanner sc = new Scanner(System.in); 
        KeyPair keypair = RSA.generateRSAKeyPair();

        socket.connect(new InetSocketAddress(ip, port), 0);
        System.out.println("Connection successful!");

        DataInputStream dataIn = new DataInputStream(socket.getInputStream()); 
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        dataOut.write(keypair.getPublic().getEncoded()); // send public key to server

        byte[] connectedPublicKeyBytes = ChatUtils.readPublicKey(dataIn);
        
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey connectedPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(connectedPublicKeyBytes));

        Thread sendMessageToServer = createThread(dataIn, dataOut, sc, keypair, connectedPublicKey, socket, "send");
        Thread getMessageFromServer = createThread(dataIn, dataOut, sc, keypair, connectedPublicKey, socket, "get");

        getMessageFromServer.start();
        sendMessageToServer.start();

        getMessageFromServer.join();
        sendMessageToServer.join();

        shutdown(dataIn, dataOut, sc, socket);
        System.exit(0);
    }

    public static Thread createThread(DataInputStream dataIn, DataOutputStream dataOut, Scanner sc, KeyPair keypair, PublicKey connectedPublicKey, Socket socket, String mode) {
        Thread getMessageFromClient = new Thread(){
            public void run() {
                String hostname = socket.getInetAddress().getHostName();
                while (true) {
                    try {
                        if (mode.equals("send")) {
                            String message = ChatUtils.sendMessage(dataOut, sc);

                            hostname = ChatUtils.parseCommands(message, dataOut, hostname);

                            if (message.equals(ChatUtils.EXIT_MESSAGE)){
                                System.out.println("exit message being sent");
                                dataOut.writeUTF("\r{Server} " + hostname + " has left the chat - use /exit to leave");
                                dataIn.close();
                                dataOut.close();
                                sc.close();
                                Server.isRunning = false;
                                return;
                            }

                            message = "\r[" + hostname + "] " + message;

                            if (!(message.startsWith("\r[" + hostname + "] /"))) { // checking if the message is a command
                                // Encrypt the message
                                byte[] encryptedMessage = RSA.encrypt(message, connectedPublicKey);
                                dataOut.write(encryptedMessage);
                                dataOut.flush();
                            }


                        } else if (mode.equals("get")) {
                            byte[] encryptedMessage = ChatUtils.getMessage(dataIn);
                            String message = RSA.decrypt(encryptedMessage, keypair.getPrivate());

                            if (message == null || message.equals(ChatUtils.EXIT_MESSAGE)){
                                return;
                            }

                            System.out.println(message);
                            System.out.print(ChatUtils.USER_PROMPT);
                        } else {
                            System.out.println("Invalid mode argument.");
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };

        return getMessageFromClient;
    }

    private static void shutdown(DataInputStream dataIn, DataOutputStream dataOut, Scanner sc, Socket socket) throws IOException {
        System.out.println("Shutting down client...");
        dataIn.close();
        dataOut.close();
        sc.close();
        socket.close();
    }
}
