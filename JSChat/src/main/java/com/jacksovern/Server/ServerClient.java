package com.jacksovern.Server;

import java.io.*;
import java.net.*;

public class ServerClient {
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private int clientID;

    /**
     * @param socket
     * @throws IOException
     */
    public ServerClient(Socket socket, int id) throws IOException {
        dataIn = new DataInputStream(socket.getInputStream());
        dataOut = new DataOutputStream(socket.getOutputStream());
        clientID = id;
    }

    public int getClientID() {
        return clientID;
    }

    public void closeAll(){
        try{
        dataIn.close();
        dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataInputStream getDataIn() {
        return dataIn;
    }

    public DataOutputStream getDataOut() {
        return dataOut;
    }

}
