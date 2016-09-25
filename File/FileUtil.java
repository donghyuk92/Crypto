package File;

import java.io.*;
import java.security.KeyPair;

/**
 * Created by slave on 2016-09-26.
 */
public class FileUtil implements Serializable {
	public void serializeDataOut(KeyPair ish) throws IOException, FileNotFoundException {
		String fileName= "savedKey.txt";
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(ish);
		oos.close();
	}

	public KeyPair serializeDataIn() throws IOException, ClassNotFoundException {
		String fileName= "savedKey.txt";
		FileInputStream fin = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		KeyPair keyPair= (KeyPair) ois.readObject();
		ois.close();
		return keyPair;
	}
}
