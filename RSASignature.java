import java.security.*;

public class RSASignature {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        kpg.initialize(1024, random);
        KeyPair keyPair = kpg.genKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] pubk = publicKey.getEncoded();
        byte[] prik = privateKey.getEncoded();

        System.out.println("\n\nRSA key generation ");
        System.out.print("\nPublic Key : ");
        for (byte b : pubk) System.out.printf("%02X ", b);
        System.out.println("\nPublic Key Length : " + pubk.length + " byte");
        System.out.print("\nPrivate Key : ");
        for (byte b : prik) System.out.printf("%02X ", b);
        System.out.println("\nPrivate Key Length : " + prik.length + " byte");

        String sigData = "Electronic Signature Test ";
        byte[] data = sigData.getBytes("UTF8");
        System.out.print("\nPlaintext : " + sigData + "\n");
        //-----------------------------------------
        System.out.println("\n\nSHA512WithRSA");
        Signature sig2 = Signature.getInstance("SHA512WithRSA");
        sig2.initSign(keyPair.getPrivate());
        sig2.update(data);
        byte[] signatureBytes2 = sig2.sign();
        System.out.print("\nSingature: ");
        for (byte b : signatureBytes2) System.out.printf("%02X ", b);
        System.out.print("\nSingature length: " + signatureBytes2.length * 8 + " bits");

        sig2.initVerify(keyPair.getPublic());
        sig2.update(data);
        System.out.print("\nVerification: ");
        System.out.print(sig2.verify(signatureBytes2));

    }
}