package Chat;

import java.io.Serializable;

/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Chat.Server.
 * When talking from a Java Client.Client to a Java Chat.Server a lot easier to pass Java objects, no
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	static final int SEND_PUB_KEY = 0, MESSAGE = 1, LOGOUT = 2, FILE = 3, SIGN = 4, SEND_SECRET_KEY = 5;
	private int type;
	private String message;
	private byte[] cipherText;
	private byte[] cipherFile;
	private byte[] cipherSecretKey;
	private byte[] encodedKey;

	// constructor
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}

	// getters
	public int getType() {
		return this.type;
	}

	public void setEncodedKey(byte[] encodedKey) {
		this.encodedKey = encodedKey;
	}

	public void setCipherText(byte[] cipherText) {
		this.cipherText = cipherText;
	}

	public void setCipherFile(byte[] cipherFile) {
		this.cipherFile = cipherFile;
	}

	public void setCipherSecretKey(byte[] cipherSecretKey) {
		this.cipherSecretKey = cipherSecretKey;
	}

	public String getMessage() {
		return message;
	}

	public byte[] getEncodedKey() {
		return this.encodedKey;
	}

	public byte[] getCipherText() {
		return this.cipherText;
	}

	public byte[] getCipherFile() {
		return this.cipherFile;
	}

	public byte[] getCipherSecretKey() {
		return this.cipherSecretKey;
	}
}