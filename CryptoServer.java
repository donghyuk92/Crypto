import javax.swing.*;

public class CryptoServer {
	public void main(String[] args) {
		// schedule this for the event dispatch thread (edt)
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CryptoUI().displayJFrame();
			}
		});
	}
}