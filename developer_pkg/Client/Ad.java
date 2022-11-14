import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.Timer;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;

public class Ad extends JDialog {
	private JLabel textField;
	private static int count = 5;
	
	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		try {
//			Ad dialog = new Ad();
//			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			dialog.setVisible(true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Create the dialog.
	 */

	public Ad(Frame owner, boolean modal) {
	//	setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
		
		
		super(owner, "Ad");
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		setUndecorated(true);
		setBounds(100, 100, 984, 603);
		getContentPane().setLayout(null);
		
		textField = new JLabel();
		textField.setFont(new Font("新細明體", Font.PLAIN, 20));
		textField.setText("5");
		textField.setForeground(Color.RED);
		textField.setBackground(Color.BLACK);
		textField.setBounds(960, 580, 36, 21);
		getContentPane().add(textField);
//		textField.setColumns(10);

//		int num = 2;
//		{
//			JLabel lblad = new JLabel("");
//			lblad.setBounds(0, 0, 984, 603);
//			String text = "C:\\Users\\user\\Desktop\\java2\\advertisement\\NewAd\\" + num + ".jpg";
//			lblad.setIcon(new ImageIcon(text));
//			getContentPane().add(lblad);
//		}
	}
	
	public Ad(Frame owner, boolean modal, int adcode) {
	//	setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
		
		
		super(owner, "Ad");
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		setUndecorated(true);
		setBounds(100, 100, 984, 603);
		getContentPane().setLayout(null);
		
		textField = new JLabel();
		textField.setFont(new Font("新細明體", Font.PLAIN, 20));
		textField.setText("5");
		textField.setForeground(Color.RED);
		textField.setBackground(Color.BLACK);
		textField.setBounds(960, 580, 36, 21);
		getContentPane().add(textField);
//		textField.setColumns(10);


		JLabel lblad = new JLabel("");
		lblad.setBounds(0, 0, 984, 603);
		String text = "C:\\Users\\user\\Desktop\\java2\\advertisement\\NewAd\\" + adcode + ".jpg";
		lblad.setIcon(new ImageIcon(text));
		getContentPane().add(lblad);
		
	}
	
	public static void start(Frame owner, boolean modal, int adcode) {
		Ad ad = new Ad(owner, modal, adcode);
		ad.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Timer timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (count == 0) {
					ad.dispose();
				} else {
					ad.textField.setText("" + count + "");
					count--;
				}
			}
		});
		timer.setInitialDelay(0);
		timer.start();
		ad.setVisible(true);	
	}

}
