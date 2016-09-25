import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by slave on 2016-09-25.
 */
public class CryptoUI {
	JFrame frame;

	void displayJFrame() {
		frame = new JFrame("Our JButton listener example");
		// create our jbutton
		JButton showDialogButton = new JButton("Click Me");
		// add the listener to the jbutton to handle the "pressed" event

		showDialogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// display/center the jdialog when the button is pressed
				JDialog d = new JDialog(frame, "Hello", true);
				d.setLocationRelativeTo(frame);
				d.setVisible(true);
			}
		});

		// put the button on the frame
		frame.getContentPane().setLayout(new FlowLayout());
		frame.add(showDialogButton);

		// set up the jframe, then display it
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(300, 200));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
