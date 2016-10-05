package Crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class SymCryption {

	public SecretKey keyGen() throws NoSuchAlgorithmException {
		System.out.println("\n\nAES Key Generation ");
		KeyGenerator keyGen2 = KeyGenerator.getInstance("AES");
		keyGen2.init(128);
		SecretKey key2 = keyGen2.generateKey();
		byte[] printKey2 = key2.getEncoded();

		System.out.print("Secret key generation complete: ");
		for (byte b : printKey2) System.out.printf("%02X ", b);
		System.out.print("\nLength of secret key: " + printKey2.length + " byte\n");

		return key2;
	}

	public byte[] encryptFile(byte[] fileBytes, SecretKey key) throws Exception {
		System.out.println("\n\nAES Encryption ");
		Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher2.init(Cipher.ENCRYPT_MODE, key);

		byte[] cipherText = cipher2.doFinal(fileBytes);

		System.out.print("\nCiphertext :");
		for (byte b : cipherText) System.out.printf("%02X ", b);
		System.out.print("\nCiphertext Length: " + cipherText.length + " byte");

		return cipherText;
	}

	public byte[] decryptFile(byte[] cipherText, SecretKey key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptText = cipher.doFinal(cipherText);
		String output2 = new String(decryptText, "UTF8");
		System.out.print("\nDecrypted Text:" + output2);
		return decryptText;
	}

	public SecretKey getSecretKey(byte[] secretKey) {
		return new SecretKeySpec(secretKey, 0, secretKey.length, "AES");
	}

	public static void main(String[] args) throws Exception {
		SymCryption symCryption = new SymCryption();
		//symCryption.key = symCryption.keyGen();
		//byte[] encryptedMessage = symCryption.encryptFile("donghyuk", symCryption.key);
		//symCryption.decryptFile(encryptedMessage, symCryption.key);
	}
}