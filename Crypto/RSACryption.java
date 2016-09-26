package Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

public class RSACryption {
	private KeyPair keyPair;

	public KeyPair keyGen() throws NoSuchAlgorithmException {
		System.out.println("\n=== RSA Key Generation ===");
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		keyPair = generator.generateKeyPair();
		return keyPair;
	}

	public void encryptMessage(String plainText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		System.out.print("\n Plaintext : " + plainText + "\n");
		byte[] t0 = plainText.getBytes();
		for (byte b : t0) System.out.printf("%02X ", b);
		System.out.println("\n Plaintext Length : " + t0.length + " byte");

		System.out.println("\n=== RSA Encryption ===");
		Cipher cipher = Cipher.getInstance("RSA");
		//cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] b0 = cipher.doFinal(t0);
		System.out.print("\n\n Ciphertext : ");
		for (byte b : b0) System.out.printf("%02X ", b);
		System.out.println("\n Ciphertext Length : " + b0.length + " byte");
	}

	public void decryptMessage(byte[] cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		System.out.println("=== RSA Decryption ===");
		Cipher cipher = Cipher.getInstance("RSA");
		//cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] b1 = cipher.doFinal(cipherText);
		System.out.print("\n Recovered Plaintext : " + new String(b1) + "\n");
		for (byte b : b1) System.out.printf("%02X ", b);
		System.out.println("\n Recovered Plaintext Length : " + b1.length + " byte");
	}

	public KeyPair getKeyPair() {
		return this.keyPair;
	}

	public PublicKey getPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedKey));
	}

	public PrivateKey getPrivateKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
	}
}