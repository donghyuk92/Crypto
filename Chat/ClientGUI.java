package Chat;

import Crypto.RSACryption;
import File.FileUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;


/*
 * The Client.Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// will first hold "Username:", later on "Enter message"
	private JLabel label;
	// to hold the Username and later on the messages
	private JTextField tf;
	// to hold the server address an the port number
	private JTextField tfServer, tfPort;
	// to Logout and get the list of the users
	private JButton login, logout, keyGen, sendPubKey, saveFile, loadFile;
	// for the chat room
	private JTextArea ta;
	// if it is for connection
	private boolean connected;
	// the Client.Client object
	private Client client;
	// the default port number
	private int defaultPort;
	private String defaultHost;

	private RSACryption cryption;
	private FileUtil fileUtil;
	private KeyPair keyPair;
	private ChatMessage chatMessage;

	// Constructor connection receiving a socket number
	ClientGUI(String host, int port) {

		super("Chat Client.Client");
		defaultPort = port;
		defaultHost = host;

		// The NorthPanel with:
		JPanel northPanel = new JPanel(new GridLayout(3, 1));
		// the server name anmd the port number
		JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));
		// the two JTextField with default value for server address and port number
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Chat.Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		// adds the Chat.Server an port field to the GUI
		northPanel.add(serverAndPort);

		// the Label and the TextField
		label = new JLabel("Enter your username below", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Anonymous");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);

		// The CenterPanel which is the chat room
		ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1, 1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// the 3 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);        // you have to login before being able to logout
		keyGen = new JButton("Key generation");
		keyGen.addActionListener(this);
		sendPubKey = new JButton("Send public key");
		sendPubKey.addActionListener(this);
		sendPubKey.setEnabled(false);
		saveFile = new JButton("save key");
		saveFile.addActionListener(this);
		saveFile.setEnabled(false);
		loadFile = new JButton("load key");
		loadFile.addActionListener(this);

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(keyGen);
		southPanel.add(sendPubKey);
		southPanel.add(saveFile);
		southPanel.add(loadFile);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600);
		setVisible(true);
		tf.requestFocus();

		cryption = new RSACryption();
		fileUtil = new FileUtil();
		chatMessage = new ChatMessage(ChatMessage.MESSAGE, "default");
	}

	// called by the Client.Client to append text in the TextArea
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}

	// called by the GUI is the connection failed
	// we reset our buttons, label, textfield
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		sendPubKey.setEnabled(false);
		label.setText("Enter your username below");
		tf.setText("Anonymous");
		// reset port number and host name as a construction time
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		// let the user change them
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		// don't react to a <CR> after the username
		tf.removeActionListener(this);
		connected = false;
	}

	/*
	* Button or JTextField clicked
	*/
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if it is the Logout button
		if (o == logout) {
			chatMessage.setMessage(ChatMessage.LOGOUT, "");
			client.sendMessage(chatMessage);
			return;
		}

		if (o == keyGen) {
			display("key generation");
			try {
				this.keyPair = cryption.keyGen();
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			}
			saveFile.setEnabled(true);
			return;
		}

		if (o == sendPubKey) {
			display("send public key");
			chatMessage.setMessage(ChatMessage.SENDKEY, "");
			chatMessage.setEncodedKey(this.keyPair.getPublic().getEncoded());

//			for (byte b : this.keyPair.getPublic().getEncoded()) System.out.printf("%02X ", b);
//			System.out.println("\n Private Key Length : " + this.keyPair.getPublic().getEncoded().length + " byte");

			client.sendMessage(chatMessage);
			return;
		}

		if (o == saveFile) {
			display("save key to file");
			try {
				fileUtil.serializeDataOut(cryption.getKeyPair());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}

		if (o == loadFile) {
			display("load key from file");
			try {
				this.keyPair = fileUtil.serializeDataIn();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}

		// ok it is coming from the JTextField
		if (connected) {
			// just have to send the message
			byte[] cipherText = null;
			try {
				cipherText = cryption.encryptMessage(tf.getText(), this.keyPair.getPrivate());
			} catch (NoSuchPaddingException e1) {
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			} catch (InvalidKeyException e1) {
				e1.printStackTrace();
			} catch (BadPaddingException e1) {
				e1.printStackTrace();
			} catch (IllegalBlockSizeException e1) {
				e1.printStackTrace();
			}
			chatMessage.setMessage(ChatMessage.MESSAGE, "");
			chatMessage.setCipherText(cipherText);
			client.sendMessage(chatMessage);
			tf.setText("");
			return;
		}


		if (o == login) {
			// ok it is a connection request
			String username = tf.getText().trim();
			// empty username ignore it
			if (username.length() == 0)
				return;
			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if (server.length() == 0)
				return;
			// empty or invalid port numer, ignore it
			String portNumber = tfPort.getText().trim();
			if (portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			} catch (Exception en) {
				return;   // nothing I can do if port number is not valid
			}

			// try creating a new Client.Client with GUI
			client = new Client(server, port, username, this);
			// test if we can start the Client.Client
			if (!client.start())
				return;
			tf.setText("");
			label.setText("Enter your message below");
			connected = true;

			// disable login button
			login.setEnabled(false);
			// enable the 2 buttons
			logout.setEnabled(true);
			sendPubKey.setEnabled(true);
			// disable the Chat.Server and Port JTextField
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener for when the user enter a message
			tf.addActionListener(this);
		}

	}

	private void display(String msg) {
		this.append(msg + "\n");        // append to the Client.ClientGUI JTextArea (or whatever)
	}

	// to start the whole thing the server
	public static void main(String[] args) {
		new ClientGUI("localhost", 1500);
	}

}