package com.jacksovern.Server;

import java.io.*;
import java.net.*;

public class ServerClient {
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    /**
     * @param socket
     * @throws IOException
     */
    public ServerClient(Socket socket) throws IOException {
        dataIn = new DataInputStream(socket.getInputStream());
        dataOut = new DataOutputStream(socket.getOutputStream());
    }

    public DataInputStream getDataIn() {
        return dataIn;
    }

    public DataOutputStream getDataOut() {
        return dataOut;
    }

}
