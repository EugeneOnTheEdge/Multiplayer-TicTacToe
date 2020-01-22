import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

public class TicTacToe_Server {
	public static void main(String[] args) {
		JFrame mainFrame = new JFrame();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(600,600);
		mainFrame.setTitle("Tic-Tac-Toe Server");

		TicTacToe_ServerComponent_NonBlocking server = new TicTacToe_ServerComponent_NonBlocking();

		mainFrame.setContentPane(server);
		mainFrame.setVisible(true);
		
		server.connectToClient();
	}
}