package Chat;

import Crypto.RSACryption;

import java.net.*;
import java.io.*;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import File.FileUtil;
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

	/*
	 *  Constructor called by console mode
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	Client(String server, int port, String username) {
		// which calls the common constructor with the GUI set to null
		this(server, port, username, null);
	}

	/*
	 * Constructor call when used from a GUI
	 * in console mode the ClienGUI parameter is null
	 */
	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		// save if we are in GUI mode or not
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

	/*
	 * To start the Client.Client in console mode use one of the following command
	 * > java Client.Client
	 * > java Client.Client username
	 * > java Client.Client username portNumber
	 * > java Client.Client username portNumber serverAddress
	 * at the console prompt
	 * If the portNumber is not specified 1500 is used
	 * If the serverAddress is not specified "localHost" is used
	 * If the username is not specified "Anonymous" is used
	 * > java Client.Client
	 * is equivalent to
	 * > java Client.Client Anonymous 1500 localhost
	 * are eqquivalent
	 *
	 * In console mode, if an error occurs the program simply stops
	 * when a GUI id used, the GUI is informed of the disconnection
	 */
//	public static void main(String[] args) {
//		// default values
//		int portNumber = 1500;
//		String serverAddress = "localhost";
//		String userName = "Anonymous";
//
//		// depending of the number of arguments provided we fall through
//		switch (args.length) {
//			// > javac Client.Client username portNumber serverAddr
//			case 3:
//				serverAddress = args[2];
//				// > javac Client.Client username portNumber
//			case 2:
//				try {
//					portNumber = Integer.parseInt(args[1]);
//				} catch (Exception e) {
//					System.out.println("Invalid port number.");
//					System.out.println("Usage is: > java Client.Client [username] [portNumber] [serverAddress]");
//					return;
//				}
//				// > javac Client.Client username
//			case 1:
//				userName = args[0];
//				// > java Client.Client
//			case 0:
//				break;
//			// invalid number of arguments
//			default:
//				System.out.println("Usage is: > java Client.Client [username] [portNumber] {serverAddress]");
//				return;
//		}
//		// create the Client.Client object
//		Client client = new Client(serverAddress, portNumber, userName);
//		// test if we can start the connection to the Chat.Server
//		// if it failed nothing we can do
//		if (!client.start())
//			return;
//
//		// wait for messages from user
//		Scanner scan = new Scanner(System.in);
//		// loop forever for message from the user
//		while (true) {
//			System.out.print("> ");
//			// read message from user
//			String msg = scan.nextLine();
//			// logout if message is LOGOUT
//			if (msg.equalsIgnoreCase("LOGOUT")) {
//				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
//				// break to do the disconnect
//				break;
//			}
//			// message WhoIsIn
//			else if (msg.equalsIgnoreCase("SENDKEY")) {
//				client.sendMessage(new ChatMessage(ChatMessage.SENDKEY, ""));
//			} else {                // default to ordinary message
//				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
//			}
//		}
//		// done disconnect
//		client.disconnect();
//	}

	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
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