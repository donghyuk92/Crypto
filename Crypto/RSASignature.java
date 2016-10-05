package Crypto;

import java.security.*;

public class RSASignature {

	public Signature getInstance() {
		Signature signature = null;
		try {
			signature = Signature.getInstance("SHA512WithRSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return signature;
	}

	public byte[] sign(Signature signature, PrivateKey privateKey, String sigData) throws Exception {
		byte[] data = sigData.getBytes("UTF8");
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	public String verify(Signature signature, byte[] signatureBytes, PublicKey publicKey, String sigData) throws Exception {
		byte[] data = sigData.getBytes("UTF8");
		signature.initVerify(publicKey);
		signature.update(data);
		System.out.print("\nVerification: ");
		System.out.print(signature.verify(signatureBytes));

		signature.initVerify(publicKey);
		signature.update(data);
		String res = null;
		if(signature.verify(signatureBytes)) {
			res = "TRUE";
		}
		else {
			res = "FALSE";
		}
		return res;
	}
}