package File;

import Crypto.KeyWrapper;

import java.io.*;
import java.security.KeyPair;

/**
 * Created by slave on 2016-09-26.
 */
public class FileUtil implements Serializable {
	public void serializeDataOut(KeyWrapper ish) throws IOException {
		String fileName= "savedKey.txt";
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(ish);
		oos.close();
	}

	public KeyWrapper serializeDataIn() throws IOException, ClassNotFoundException {
		String fileName= "savedKey.txt";
		FileInputStream fin = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		KeyWrapper keyPair= (KeyWrapper) ois.readObject();
		ois.close();
		return keyPair;
	}

	public void serializeDataOutForServer(KeyWrapper ish) throws IOException {
		String fileName= "savedKeyForServer.txt";
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(ish);
		oos.close();
	}

	public KeyWrapper serializeDataInForServer() throws IOException, ClassNotFoundException {
		String fileName= "savedKeyForServer.txt";
		FileInputStream fin = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		KeyWrapper keyPair= (KeyWrapper) ois.readObject();
		ois.close();
		return keyPair;
	}
}
