package Chat;

import Crypto.RSACryption;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;

import File.FileUtil;

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

	private RSACryption cryption;
	private FileUtil fileUtil;

	private KeyPair keyPair;
	private PublicKey userPublicKey;

	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */
	public Server(int port) {
		this(port, null);
	}

	public Server(int port, ServerGUI sg) {
		// GUI or not
		this.sg = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client.Client list
		al = new ArrayList<ClientThread>();

		this.cryption = new RSACryption();
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
			messageLf = cryption.encryptMessage(plainText, userPublicKey);
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

	/*
	 *  To run as a console application just open a console window and:
	 * > java Chat.Server
	 * > java Chat.Server portNumber
	 * If the port number is not specified 1500 is used
	 */
//	public static void main(String[] args) {
//		// start server on port 1500 unless a PortNumber is specified
//		int portNumber = 1500;
//		switch (args.length) {
//			case 1:
//				try {
//					portNumber = Integer.parseInt(args[0]);
//				} catch (Exception e) {
//					System.out.println("Invalid port number.");
//					System.out.println("Usage is: > java Chat.Server [portNumber]");
//					return;
//				}
//			case 0:
//				break;
//			default:
//				System.out.println("Usage is: > java Chat.Server [portNumber]");
//				return;
//
//		}
//		// create a server object and start it
//		Server server = new Server(portNumber);
//		server.start();
//	}

	/**
	 * One instance of this thread will run for each client
	 */
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
				System.out.println(cm.getType());
				System.out.println(message);
				switch (cm.getType()) {
					case ChatMessage.MESSAGE:
						byte[] plainText = null;
						try {
							plainText = cryption.decryptMessage(cm.getCipherText(), keyPair.getPrivate());
							System.out.println(plainText);
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
					case ChatMessage.SENDKEY:
						try {
							userPublicKey = cryption.getPublicKey(cm.getEncodedKey());
						} catch (Exception e) {
							e.printStackTrace();
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
			;
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
			this.keyPair = cryption.keyGen();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
	}

	public void sendPubKey() {
		ChatMessage chatMessage = new ChatMessage(ChatMessage.SENDKEY, "");
		chatMessage.setEncodedKey(keyPair.getPublic().getEncoded());
		for (int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			ct.writeObject(chatMessage);
		}
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
}