import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    private static SecretKey AESKey;
    private static String ipAddress = "149.130.213.82";

    public static void main (String[] args) throws Exception {
        if (args.length >= 2) {
            if (args[0].equals("custom")) {
                ipAddress = args[1];
            }
        } 

        Client.clientMode(ipAddress, ChatUtils.portNumber);
    }

    public static void clientMode (String ip, int port) throws Exception {
        Socket socket = new Socket();
        Scanner sc = new Scanner(System.in); 
        KeyPair keypair = RSA.generateRSAKeyPair();
        AESKey = AES.generateKey(128);

        socket.connect(new InetSocketAddress(ip, port), 0);
        System.out.println("Connection successful!");

        DataInputStream dataIn = new DataInputStream(socket.getInputStream()); 
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        dataOut.write(keypair.getPublic().getEncoded()); // send public key to server

        byte[] connectedPublicKeyBytes = ChatUtils.readKeyBytes(dataIn, 422);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey connectedPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(connectedPublicKeyBytes));

        dataOut.write(RSA.encrypt(AESKey.getEncoded(), connectedPublicKey)); // send AES key to server

        byte[] AESKeyBytes = new byte[384];
        if (dataIn.read(AESKeyBytes) < 384){
            System.out.println("AES key not received in full.");
        }

        try {
            AESKey = new SecretKeySpec(RSA.decrypt(AESKeyBytes, keypair.getPrivate()), "AES");
        } catch (Exception ignored) {}

        Thread sendMessageToServer = createThread(keypair, connectedPublicKey, socket, "send");
        Thread getMessageFromServer = createThread(keypair, connectedPublicKey, socket, "get");

        getMessageFromServer.start();
        sendMessageToServer.start();

        getMessageFromServer.join();
        sendMessageToServer.join();

        ChatUtils.shutdown(dataIn, dataOut, null, null, sc, socket, null, null);
    }

    public static Thread createThread(KeyPair keypair, PublicKey connectedPublicKey, Socket socket, String mode) throws IOException {
        Thread getMessageFromClient = new Thread(){
            public void run() {
                try{
                    DataInputStream dataIn = new DataInputStream(socket.getInputStream()); 
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    String hostname = socket.getInetAddress().getHostName();
                    Scanner scanner = new Scanner(System.in);

                    switch (mode){
                        case "send":
                            while (true){
                                hostname = ChatUtils.sendMessageToServer(scanner, dataIn, dataOut, AESKey, hostname);
                                if (hostname == null){
                                    return;
                                }
                            }
                        case "get":
                            while (true) {
                                if (!ChatUtils.getMessageFromServer(scanner, dataIn, dataOut, AESKey)){
                                    return;
                                }
                            }
                        default:
                            System.out.println("Invalid mode argument.");
                            scanner.close();
                            dataIn.close();
                            dataOut.close();
                            return;
                    }
                } catch (
                IOException | 
                IllegalBlockSizeException | 
                BadPaddingException | 
                InvalidKeyException | 
                InvalidAlgorithmParameterException | 
                NoSuchAlgorithmException | 
                NoSuchPaddingException e ) {
                    return;
                }
            }
        };

        return getMessageFromClient;
    }
}

