package com.jacksovern.Client;

import java.io.DataInputStream;
import java.io.IOException;

import javax.crypto.SecretKey;

public class MessageReceiver {
    private DataInputStream dataIn;
    private SecretKey AESKey;

    public MessageReceiver(DataInputStream dataIn, SecretKey AESKey) {
        this.dataIn = dataIn;
        this.AESKey = AESKey;
    }

    public String getMessage() {
        System.out.print("\r[you] ");
        byte[] encryptedBytes = null;
        String message = null;

        try {
            encryptedBytes = getEncryptedMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        message = AES.decrypt(encryptedBytes, AESKey);

        return message;
    }

    /**
     * @return the encrypted bytes of the message
     * @throws IOException
     */
    private byte[] getEncryptedMessage() throws IOException {
        int messageLength = dataIn.readInt();
        byte[] encryptedBytes = new byte[messageLength];

        int bytesRead = 0;

        while (bytesRead < messageLength) {
            // read returns the number of the bytes read or -1 if there is an error
            int result = dataIn.read(encryptedBytes, bytesRead, messageLength - bytesRead);
            if (result == -1) {
                throw new IOException("End of stream reached before message was complete");
            }

            bytesRead += result;
        }

        return encryptedBytes;
    }
}
