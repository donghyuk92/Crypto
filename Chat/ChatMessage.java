package Chat;

import java.io.*;

/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Chat.Server.
 * When talking from a Java Client.Client to a Java Chat.Server a lot easier to pass Java objects, no
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The different types of message sent by the Client.Client
	// SENDKEY to receive the list of the users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from the Chat.Server
	static final int SENDKEY = 0, MESSAGE = 1, LOGOUT = 2;
	private int type;
	private String message;
	private byte[] cipherText;
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

	public void setMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}

	public void setEncodedKey(byte[] encodedKey) {
		this.encodedKey = encodedKey;
	}

	public void setCipherText(byte[] cipherText) {
		this.cipherText = cipherText;
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
}