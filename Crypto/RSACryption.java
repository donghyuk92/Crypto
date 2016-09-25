package Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.Scanner;

public class RSACryption {
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();


        System.out.println("\n=== RSA Key Generation ===");
        byte[] pubk = publicKey.getEncoded();
        byte[] prik = privateKey.getEncoded();
        System.out.print("\n Public Key : ");
        for (byte b : pubk) System.out.printf("%02X ", b);
        System.out.println("\n Public Key Length : " + pubk.length + " byte");
        System.out.print("\n Private Key : ");
        for (byte b : prik) System.out.printf("%02X ", b);
        System.out.println("\n Private Key Length : " + prik.length + " byte");

        System.out.println("\n=== RSA Encryption ===");
        Scanner s = new Scanner(System.in);
        System.out.print("Input the plaintext to be encrypted... = ");
        String text = s.next();
        byte[] t0 = text.getBytes();
        System.out.print("\n Plaintext : "+text+"\n");
        for(byte b: t0) System.out.printf("%02X ", b);
        System.out.println("\n Plaintext Length : "+t0.length+ " byte" );

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] b0 = cipher.doFinal(t0);
        System.out.print("\n\n Ciphertext : ");
        for(byte b: b0) System.out.printf("%02X ", b);
        System.out.println("\n Ciphertext Length : "+b0.length+ " byte" );

        System.out.println("=== RSA Decryption ===");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] b1 = cipher.doFinal(b0);
        System.out.print("\n Recovered Plaintext : "+ new String(b1) +"\n");
        for(byte b: b1) System.out.printf("%02X ", b);
        System.out.println("\n Recovered Plaintext Length : "+b1.length+ " byte" );
    }
}