package Chat;

import Crypto.RSACryption;
import File.FileUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
/*
 * The Client.Client that can be run both as a console or a GUI
 */
public class Client {

	// for I/O
	private ObjectInputStream sInput;        // to read from the socket
	private ObjectOutputStream sOutput;        // to write on the socket
	private Socket socket;

	// if I use a GUI or not
	private ClientGUI cg;

	// the server, the port and the username
	private String server, username;
	private int port;

	private RSACryption cryption;
	private FileUtil fileUtil;
	private KeyPair keyPair;
	private PublicKey serverPubKey;

	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg;

		this.cryption = new RSACryption();
		this.fileUtil = new FileUtil();
	}

	/*
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		}
		// if it failed not much I can so
		catch (Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		/* Creating both Data Stream */
		try {
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be Chat.ChatMessage objects
		try {
			sOutput.writeObject(username);
		} catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
		if (cg == null)
			System.out.println(msg);      // println in console mode
		else
			cg.append(msg + "\n");        // append to the Client.ClientGUI JTextArea (or whatever)
	}

	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		} catch (IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try {
			if (sInput != null) sInput.close();
		} catch (Exception e) {
		} // not much else I can do
		try {
			if (sOutput != null) sOutput.close();
		} catch (Exception e) {
		} // not much else I can do
		try {
			if (socket != null) socket.close();
		} catch (Exception e) {
		} // not much else I can do

		// inform the GUI
		if (cg != null)
			cg.connectionFailed();

	}

	class ListenFromServer extends Thread {

		public void run() {
			while (true) {
				try {
					ChatMessage msg = (ChatMessage) sInput.readObject();
					// if console mode print the message and add back the prompt
					if (cg == null) {
						System.out.println(msg);
						System.out.print("> ");
					} else {
						switch (msg.getType()) {
							case ChatMessage.SENDKEY:
								setServerPubKey(cryption.getPublicKey(msg.getEncodedKey()));
								break;
							case ChatMessage.MESSAGE:
								byte[] plainText = null;
								try {
									plainText = cryption.decryptMessage(msg.getCipherText(), keyPair.getPrivate());
									cg.append(new String(plainText));
								} catch (Exception e) {
									e.printStackTrace();
								}
						}
					}
				} catch (IOException e) {
					display("Chat.Server has close the connection: " + e);
					if (cg != null)
						cg.connectionFailed();
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch (ClassNotFoundException e2) {
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void logout() {
		ChatMessage chatMessage = new ChatMessage(ChatMessage.LOGOUT, "");
		sendMessage(chatMessage);
	}

	public void keyGen() {
		try {
			this.keyPair = cryption.keyGen();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
	}

	public void sendPubKey() {
		ChatMessage chatMessage = new ChatMessage(ChatMessage.SENDKEY, "");
		chatMessage.setEncodedKey(this.keyPair.getPublic().getEncoded());
		sendMessage(chatMessage);
	}

	public void sendEncryptMessage() {
		byte[] cipherText = null;
		try {
			cipherText = cryption.encryptMessage(cg.tf.getText(), serverPubKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ChatMessage chatMessage = new ChatMessage(ChatMessage.MESSAGE, "");
		chatMessage.setCipherText(cipherText);
		sendMessage(chatMessage);
	}

	public void saveFile() {
		try {
			fileUtil.serializeDataOut(cryption.getKeyPair());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void loadFile() {
		try {
			this.keyPair = fileUtil.serializeDataIn();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void setServerPubKey(PublicKey serverPubKey) {
		this.serverPubKey = serverPubKey;
	}
}