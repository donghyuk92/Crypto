package Crypto;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;

/**
 * Created by slave on 2016-09-29.
 */
public class KeyWrapper implements Serializable {
	public KeyPair keyPair;
	public PublicKey publicKey;
	public SecretKey secretKey;

	public KeyWrapper(KeyPair keyPair, PublicKey publicKey, SecretKey secretKey) {
		this.keyPair = keyPair;
		this.publicKey = publicKey;
		this.secretKey = secretKey;
	}
}
