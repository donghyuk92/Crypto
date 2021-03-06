package Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class RSACryption {

	public KeyPair keyGen() throws NoSuchAlgorithmException {
		System.out.println("\n=== RSA Key Generation ===");
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		return generator.generateKeyPair();
	}

	public byte[] encryptMessage(String plainText, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		System.out.print("\n Plaintext : " + plainText + "\n");
		byte[] t0 = plainText.getBytes();
		for (byte b : t0) System.out.printf("%02X ", b);
		System.out.println("\n Plaintext Length : " + t0.length + " byte");

		System.out.println("\n=== RSA Encryption ===");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] b0 = cipher.doFinal(t0);
		System.out.print("\n\n Ciphertext : ");
		for (byte b : b0) System.out.printf("%02X ", b);
		System.out.println("\n Ciphertext Length : " + b0.length + " byte");

		return b0;
	}

	public byte[] encryptMessageForByte(byte[] plainBytes, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		for (byte b : plainBytes) System.out.printf("%02X ", b);
		System.out.println("\n Plaintext Length : " + plainBytes.length + " byte");

		System.out.println("\n=== RSA Encryption ===");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] b0 = cipher.doFinal(plainBytes);
		System.out.print("\n\n Ciphertext : ");
		for (byte b : b0) System.out.printf("%02X ", b);
		System.out.println("\n Ciphertext Length : " + b0.length + " byte");

		return b0;
	}

	public byte[] decryptMessage(byte[] cipherText, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		System.out.println("=== RSA Decryption ===");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] b1 = cipher.doFinal(cipherText);
		System.out.print("\n Recovered Plaintext : " + new String(b1) + "\n");
		for (byte b : b1) System.out.printf("%02X ", b);
		System.out.println("\n Recovered Plaintext Length : " + b1.length + " byte");

		return b1;
	}

	public PublicKey getPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedKey));
	}
}