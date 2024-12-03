import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;


class AES {
    /*
        String input = "adsfjkhlasfhdkalsjhflafkjhaslkfhaslkdfjhsflkajhflkasdfhjaldskjfahfdkl";
        SecretKey key = generateKey(128);
        byte[] cipherText = encrypt(input, key);
        String plainText = decrypt(cipherText, key);
        System.out.println("Decrypted: " + plainText);
    */

    public final static String algorithm = "AES/CBC/PKCS5Padding";

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static byte[] encrypt(String input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException, InvalidKeyException,
    BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, generateIv());

        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        byte[] iv = cipher.getIV();
        byte[] combinedPayload = new byte[iv.length + encryptedBytes.length];

        //populate payload with prefix IV and encrypted data
        System.arraycopy(iv, 0, combinedPayload, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combinedPayload, iv.length, encryptedBytes.length);

        return combinedPayload;
    }

    public static String decrypt(byte[] cipherText, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException, InvalidKeyException,
    BadPaddingException, IllegalBlockSizeException {

        String decryptedText = "";

        try {
            //separate prefix with IV from the rest of encrypted data
            byte[] encryptedPayload = cipherText;
            byte[] iv = new byte[16];
            byte[] encryptedBytes = new byte[encryptedPayload.length - iv.length];

            //populate iv with bytes:
            System.arraycopy(encryptedPayload, 0, iv, 0, 16);

            //populate encryptedBytes with bytes:
            System.arraycopy(encryptedPayload, iv.length, encryptedBytes, 0, encryptedBytes.length);

            Cipher decryptCipher = Cipher.getInstance(algorithm);
            decryptCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] decryptedBytes = decryptCipher.doFinal(encryptedBytes);
            decryptedText = new String(decryptedBytes);

        } catch (NoSuchAlgorithmException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return decryptedText;
    }
}