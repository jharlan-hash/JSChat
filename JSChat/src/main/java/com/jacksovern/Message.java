package com.jacksovern;

public class Message{
    private int messageLength;
    private byte[] messageBytes;
    private int clientID;

    public Message (byte[] message, int clientID) {
        this.messageBytes = message;
        this.messageLength = message.length;
        this.clientID = clientID;
    }

    public int getClientID() {
        return clientID;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }
}
