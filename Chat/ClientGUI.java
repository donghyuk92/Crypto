package Chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/*
 * The Client.Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// will first hold "Username:", later on "Enter message"
	private JLabel label;
	// to hold the Username and later on the messages
	public JTextField tf;
	// to hold the server address an the port number
	private JTextField tfServer, tfPort;
	// to Logout and get the list of the users
	private JButton login, logout, keyGen, sendPubKey, sendSign, saveFile, loadFile, transFile;
	// for the chat room
	private JTextArea ta;
	// if it is for connection
	private boolean connected;
	// the Client.Client object
	private Client client;
	// the default port number
	private int defaultPort;
	private String defaultHost;

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
		sendSign = new JButton("Send signature");
		sendSign.addActionListener(this);
		sendSign.setEnabled(false);
		saveFile = new JButton("Save key");
		saveFile.addActionListener(this);
		saveFile.setEnabled(false);
		loadFile = new JButton("Load key");
		loadFile.addActionListener(this);
		transFile = new JButton("Transfer file");
		transFile.addActionListener(this);
		transFile.setEnabled(false);

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(keyGen);
		southPanel.add(sendPubKey);
		southPanel.add(sendSign);
		southPanel.add(saveFile);
		southPanel.add(loadFile);
		southPanel.add(transFile);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1000, 600);
		setVisible(true);
		tf.requestFocus();
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
		sendSign.setEnabled(false);
		transFile.setEnabled(false);
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
			display("logout");
			client.logout();
			return;
		}

		if (o == keyGen) {
			display("key generation");
			client.keyGen();
			saveFile.setEnabled(true);
			return;
		}

		if (o == sendPubKey) {
			display("send public key");
			client.sendPubKey();
			return;
		}

		if (o == sendSign) {
			display("send signature");
			client.sendSign();
			return;
		}

		if (o == saveFile) {
			display("save key to file");
			client.saveFile();
			return;
		}

		if (o == loadFile) {
			display("load key from file");
			client.loadFile();
			return;
		}

		if (o == transFile) {
			display("transfer file");
			client.transFile();
			return;
		}

		// ok it is coming from the JTextField
		if (connected) {
			// just have to send the message
			client.sendEncryptMessage();
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
			logout.setEnabled(true);
			sendPubKey.setEnabled(true);
			sendSign.setEnabled(true);
			transFile.setEnabled(true);
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