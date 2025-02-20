package com.jacksovern;

import java.util.Scanner;
import com.jacksovern.Client.Client;
import com.jacksovern.Server.Server;

public class JSChat {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(
                "Enter 'server' to start a server or 'client' and a server ip (optional) to start a client session: ");
        String input = scanner.nextLine();
        try {
            if (input.startsWith("s")) {
                new Server(1000);
            } else if (input.startsWith("c")) {
                String[] parts = input.split(" ");
                if (parts.length == 2) {
                    String ip = parts[1];
                    Client.main(new String[] { ip });
                } else {
                    Client.main(new String[] {});
                }
            } else {
                System.out.println("Invalid input.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }

    }
}
