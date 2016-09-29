package Crypto;

import java.security.*;

public class RSASignature {
    private Signature signature;

    public RSASignature() {
        this("SHA512WithRSA");
    }

    RSASignature(String hash) {
        try {
            this.signature = Signature.getInstance(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public KeyPair keyGen() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        kpg.initialize(2048, random);
        return kpg.genKeyPair();
    }

    public byte[] sign(Signature signature, PrivateKey privateKey, String sigData) throws Exception {
        byte[] data = sigData.getBytes("UTF8");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public boolean verify(Signature signature, byte[] signatureBytes, PublicKey publicKey, String sigData) throws Exception {
        byte[] data = sigData.getBytes("UTF8");
        signature.initVerify(publicKey);
        signature.update(data);
        System.out.print("\nVerification: ");
        System.out.print(signature.verify(signatureBytes));
        return signature.verify(signatureBytes);
    }

    public static void main(String[] args) throws Exception {
        RSASignature rsaSignature = new RSASignature();
        KeyPair keyPair = rsaSignature.keyGen();
        byte[] signResult = rsaSignature.sign(rsaSignature.signature, keyPair.getPrivate(), "donghyuk");
        rsaSignature.verify(rsaSignature.signature, signResult, keyPair.getPublic(), "donghyuk");
    }
}