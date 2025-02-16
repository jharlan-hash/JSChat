package com.jacksovern.Server;

public class Message{
    private int messageLength;
    private byte[] messageBytes;

    public Message (byte[] message) {
        messageBytes = message;
        messageLength = message.length;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }
}
