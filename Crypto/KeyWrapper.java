package Crypto;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PublicKey;

/**
 * Created by slave on 2016-09-29.
 */
public class KeyWrapper implements Serializable {
    public KeyPair keyPair;
    public PublicKey publicKey;

    public KeyWrapper(KeyPair keyPair, PublicKey publicKey) {
        this.keyPair = keyPair;
        this.publicKey = publicKey;
    }
}
