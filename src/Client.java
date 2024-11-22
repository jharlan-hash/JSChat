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

        byte[] connectedPublicKeyBytes = ChatUtils.readPublicKeyBytes(dataIn);
        
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
                            String message = ChatUtils.promptUserInput(dataOut, sc);

                            hostname = ChatUtils.handleUserCommands(message, dataOut, hostname, connectedPublicKey);

                            if (message.equals(ChatUtils.EXIT_MESSAGE)){
                                dataOut.write(RSA.encrypt("\r{Server} " + hostname + " has left the chat - use /exit to leave", connectedPublicKey));
                                dataIn.close();
                                dataOut.close();
                                sc.close();
                                try{
                                    ChatUtils.serverIsRunning = false;
                                } catch (Exception ignored) { return; } 
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
                            byte[] encryptedMessage = ChatUtils.receiveEncryptedMessage(dataIn);
                            String message;
                            try {
                                message = RSA.decrypt(encryptedMessage, keypair.getPrivate());
                            } catch (Exception ignored) { return; }

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
