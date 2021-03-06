package Chat;

import Crypto.KeyWrapper;
import Crypto.RSACryption;
import Crypto.RSASignature;
import Crypto.SymCryption;
import FileUtil.FileUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client.Client
	private ArrayList<ClientThread> al;
	// if I am in a GUI
	private ServerGUI sg;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;

	private RSACryption rsaCryption;
	private SymCryption symCryption;
	private RSASignature rsaSignature;
	private FileUtil fileUtil;

	private KeyPair keyPair;
	private KeyPair keyPairForSign;
	private PublicKey userPublicKey;
	private SecretKey secretKey;
	private Signature signature;

	public Server(int port, ServerGUI sg) {
		// GUI or not
		this.sg = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client.Client list
		al = new ArrayList<ClientThread>();

		this.rsaCryption = new RSACryption();
		this.symCryption = new SymCryption();
		this.rsaSignature = new RSASignature();
		this.fileUtil = new FileUtil();
	}

	public void start() {
		keepGoing = true;
		/* create socket server and wait for connection requests */
		try {
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
			while (keepGoing) {
				// format message saying we are waiting
				display("Chat.Server waiting for Clients on port " + port + ".");

				Socket socket = serverSocket.accept();    // accept connection
				// if I was asked to stop
				if (!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);  // make a thread of it
				al.add(t);                                    // save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for (int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					} catch (IOException ioE) {
						// not much I can do
					}
				}
			} catch (Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}

	/*
	 * For the GUI to stop the server
	 */
	protected void stop() {
		keepGoing = false;
		// connect to myself as Client.Client to exit statement
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		} catch (Exception e) {
			// nothing I can really do
		}
	}

	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if (sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}

	/*
	 *  to broadcast a message to all Clients
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String plainText = time + " " + message + "\n";
		byte[] messageLf = new byte[0];
		try {
			messageLf = rsaCryption.encryptMessage(plainText, userPublicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// display message on console or GUI
		if (sg == null)
			System.out.print(plainText);
		else
			sg.appendRoom(plainText);     // append in the room window

		// we loop in reverse order in case we would have to remove a Client.Client
		// because it has disconnected
		ChatMessage chatMessage = new ChatMessage(ChatMessage.MESSAGE, "");
		chatMessage.setCipherText(messageLf);

		for (int i = al.size(); --i >= 0; ) {
			ClientThread ct = al.get(i);
			// try to write to the Client.Client if it fails remove it from the list
			if (!ct.writeObject(chatMessage)) {
				al.remove(i);
				display("Disconnected Client.Client " + ct.username + " removed from list.");
			}
		}
	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for (int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if (ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}

	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client.Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;

		// Constructore
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			try {
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				display(username + " just connected.");
			} catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}

		// what will run forever
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while (keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				} catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;
				} catch (ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the Chat.ChatMessage
				String message = cm.getMessage();

				// Switch on the type of message receive
				switch (cm.getType()) {
					case ChatMessage.MESSAGE:
						byte[] plainText = null;
						try {
							plainText = rsaCryption.decryptMessage(cm.getCipherText(), keyPair.getPrivate());
							System.out.println(new String(plainText));
						} catch (Exception e) {
							e.printStackTrace();
						}
						broadcast(username + ": " + new String(plainText));
						break;
					case ChatMessage.LOGOUT:
						display(username + " disconnected with a LOGOUT message.");
						keepGoing = false;
						break;
					case ChatMessage.SEND_PUB_KEY:
						try {
							userPublicKey = rsaCryption.getPublicKey(cm.getEncodedKey());
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case ChatMessage.SEND_SECRET_KEY:
						if(secretKey == null) {
							display("receive secretKey");
							try {
								secretKey = symCryption.getSecretKey(rsaCryption.decryptMessage(cm.getCipherSecretKey(), keyPair.getPrivate()));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
					case ChatMessage.SIGN:
						byte[] decryptedBytes = null;
						try {
							decryptedBytes = symCryption.decryptFile(cm.getCipherFile(), secretKey);
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (decryptedBytes != null) {
							if(signature == null) {
								signature = rsaSignature.getInstance();
							}
							try {
								String result = rsaSignature.verify(signature, decryptedBytes, userPublicKey, username);
								display(username + " verification " +result);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
					case ChatMessage.FILE:
						byte[] decryptedBytes2 = null;
						try {
							decryptedBytes2 = symCryption.decryptFile(cm.getCipherFile(), secretKey);
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (decryptedBytes2 != null) {
							try {
								fileUtil.serializeFileOut(decryptedBytes2);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}

		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if (sOutput != null) sOutput.close();
			} catch (Exception e) {
			}
			try {
				if (sInput != null) sInput.close();
			} catch (Exception e) {
			}
			try {
				if (socket != null) socket.close();
			} catch (Exception e) {
			}
		}

		/*
		 * Write a String to the Client.Client output stream
		 */

		private boolean writeObject(Object msg) {
			// if Client.Client is still connected send the message to it
			if (!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch (IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}

	public void keyGen() {
		try {
			keyPair = rsaCryption.keyGen();
			String keyPrint = "";
			for (byte b : keyPair.getPublic().getEncoded()) {
				keyPrint += b;
				System.out.printf("%02X ", b);
			}
			display("\n Public Key : " + keyPrint);
			display("\n Public Key Length : " + keyPair.getPublic().getEncoded().length + " byte");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
	}

	public void sendPubKey() {
		ChatMessage chatMessage = new ChatMessage(ChatMessage.SEND_PUB_KEY, "");
		chatMessage.setEncodedKey(keyPair.getPublic().getEncoded());
		for (int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			ct.writeObject(chatMessage);
		}
	}

	public void saveFile() {
		try {
			fileUtil.serializeDataOutForServer(new KeyWrapper(keyPair, userPublicKey, secretKey));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void loadFile() {
		try {
			KeyWrapper keyWrapper = fileUtil.serializeDataInForServer();
			keyPair = keyWrapper.keyPair;
			userPublicKey = keyWrapper.publicKey;
			secretKey = keyWrapper.secretKey;
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}