import java.io.IOException;

public class JarDrop {
    public static void main (String[] args) throws IOException, InterruptedException {
        String mode = "";
        String ip;
        int port;

        if (args.length == 3){
            mode = args[0];
            ip = args[1];
            port = Integer.parseInt(args[2]);
        } else {
            port = -1;
            ip = "err";
            System.out.println("Please enter three arguments - mode (con or srv), ip, and port");
            System.exit(1);
        }

        if (mode.contains("con")){
            System.out.println("Acting as a client");
            Client.clientMode(ip, port);
        } else if (mode.contains("serv") || mode.contains("srv")){
            System.out.println("Acting as a server");
            Server.serverMode(ip, port);
        } else {
            System.out.println("Too many arguments supplied");
        }
    }
}
