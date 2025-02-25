package com.jacksovern.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.crypto.SecretKey;

public class MessageSender {
    private static final String USER_PROMPT = "[you] ";
    private DataOutputStream out;
    private SecretKey key;
    private Scanner scanner;

    private String username;

    public MessageSender(DataOutputStream out, SecretKey key) {
        this.out = out;
        this.key = key;
        this.scanner = new Scanner(System.in);

        username = getRealName();
    }

    public void send(String message) {
        message = "\r[" + username + "] " + message;
        byte[] encryptedMessage = AES.encrypt(message, key);

        try {
            out.writeInt(encryptedMessage.length);
            out.write(encryptedMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        System.out.print(USER_PROMPT);

        String message = scanner.nextLine();
        message = message.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
        executeCommands(message);
        return message;
    }

    public void executeCommands(String message) {
        if (message.startsWith("/nick")) {
            changeUsername(message);
        } else if (message.equals("/exit")) {
            closeAll();
        } else if (message.startsWith("/")) {
            System.out.println("Invalid command.");
            System.out.println("Commands: /exit, /nick <newUsername>");
        }
    }

    private String getRealName() {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("id", "-F");
            builder.directory(new File(System.getProperty("user.home")));

            Process process = builder.start();

            // return output of the command into a string
            Future<String> future = executorService.submit(() -> {
                StringBuilder output = new StringBuilder();
                try (DataInputStream dataIn = new DataInputStream(process.getInputStream())) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = dataIn.read(buffer)) != -1) {
                        output.append(new String(buffer, 0, bytesRead));
                    }
                }
                return output.toString();
            });

            return future.get().trim();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void closeAll() {
        scanner.close();
        System.out.println("Closing connection...");
    }

    private void changeUsername(String message) {
        String[] messageArray = message.split(" ");
        String previousName = username;

        if (messageArray.length != 2) {
            System.out.println("Invalid /nick usage.");
            System.out.println("Usage: /nick <newNickname>");
        } else {
            username = messageArray[1];
        }

        try {
            byte[] nicknameNotification = AES
                    .encrypt("\r{Server} " + previousName + " changed their nickname to " + username, key);
            out.writeInt(nicknameNotification.length);
            out.write(nicknameNotification);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
