import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

public class TicTacToe_Client {
	public static void main(String[] args) {
		JFrame mainFrame = new JFrame();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(600,600);
		mainFrame.setTitle("Tic-Tac-Toe Client");

		TicTacToe_ClientComponent client = new TicTacToe_ClientComponent();

		mainFrame.setContentPane(client);
		mainFrame.setVisible(true);
	}
}