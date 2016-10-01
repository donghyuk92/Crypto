package Crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class SymCryption {

	public void keyGen() throws NoSuchAlgorithmException {
		System.out.println("\n\nAES Key Generation ");
		KeyGenerator keyGen2 = KeyGenerator.getInstance("AES");
		keyGen2.init(128);
		Key key2 = keyGen2.generateKey();
		byte[] printKey2 = key2.getEncoded();

		System.out.print("Secret key generation complete: ");
		for (byte b : printKey2) System.out.printf("%02X ", b);
		System.out.print("\nLength of secret key: " + printKey2.length + " byte");
	}

	public void encryptMessage(String plainText, Key key) throws Exception {
		System.out.print("Plaintext : ");
		byte[] textBytes = plainText.getBytes();
		for (byte b : textBytes) System.out.printf("%02X ", b);
		System.out.print("\nPlaintext Length: " + textBytes.length + " byte");

		System.out.println("\n\nAES Encryption ");
		Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher2.init(Cipher.ENCRYPT_MODE, key);

		byte[] cipherText = cipher2.doFinal(textBytes);

		System.out.print("\nCiphertext :");
		for (byte b : cipherText) System.out.printf("%02X ", b);
		System.out.print("\nCiphertext Length: " + cipherText.length + " byte");
	}

	public void decryptMessage(byte[] cipherText, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptText = cipher.doFinal(cipherText);
		String output2 = new String(decryptText, "UTF8");
		System.out.print("\nDecrypted Text:" + output2);
	}
}