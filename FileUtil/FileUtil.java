package FileUtil;

import Crypto.KeyWrapper;

import java.io.*;

/**
 * Created by slave on 2016-09-26.
 */
public class FileUtil implements Serializable {
	public void serializeDataOut(KeyWrapper ish) throws IOException {
		String fileName = "./savedKey.txt";
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(ish);
		oos.close();
	}

	public KeyWrapper serializeDataIn() throws IOException, ClassNotFoundException {
		String fileName = "./savedKey.txt";
		FileInputStream fin = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		KeyWrapper keyPair = (KeyWrapper) ois.readObject();
		ois.close();
		return keyPair;
	}

	public void serializeDataOutForServer(KeyWrapper ish) throws IOException {
		String fileName = "./savedKeyForServer.txt";
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(ish);
		oos.close();
	}

	public KeyWrapper serializeDataInForServer() throws IOException, ClassNotFoundException {
		String fileName = "./savedKeyForServer.txt";
		FileInputStream fin = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		KeyWrapper keyPair = (KeyWrapper) ois.readObject();
		ois.close();
		return keyPair;
	}

	public void serializeFileOut(byte[] ish) throws IOException {
		String fileName = "./savedFile.txt";
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(ish);
		fos.close();
	}

	public byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
			&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}
		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}
		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
}
