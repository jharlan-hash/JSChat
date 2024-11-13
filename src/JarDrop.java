import java.io.IOException;

public class JarDrop {
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
            Client.client_mode(ip, port);
        } else if (commandName.contains("serv") || commandName.contains("srv")){
            System.out.println("Acting as a server");
            Server.server_mode(ip, port);
        } else {
            System.out.println("Too many arguments supplied");
        }
    }
}
