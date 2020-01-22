/**
	DEPRECATED!
**/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Networking Library
import java.net.*;
import java.io.*;

public class TicTacToe_ServerComponent extends JComponent {

	private final int ROW = 3;
	private final int COLUMN = 3;

	private TicTacToe_Button buttons[][]; // X,Y
	private String playerName;
	private String opponentName;
	private boolean gameStarted;

	private JLabel statusBarSecondary;
	private JLabel statusBarPrimary;
	private JTextField txtField;

	private ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dIn;
	private DataOutputStream dOut;

	public TicTacToe_ServerComponent() {
		// Initialize layout and game variables
		this.buttons = new TicTacToe_Button[COLUMN][ROW];
		this.gameStarted = false;

		this.statusBarSecondary = new JLabel("Welcome to Multiplayer Tic-Tac-Toe!");
		this.statusBarPrimary = new JLabel("Waiting for an incoming connection from your friend...");
		this.statusBarPrimary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
		this.statusBarSecondary.setHorizontalAlignment(SwingConstants.CENTER);
		this.statusBarPrimary.setHorizontalAlignment(SwingConstants.CENTER);
		this.txtField = new JTextField();
		this.txtField.setHorizontalAlignment(SwingConstants.CENTER);
		this.txtField.setEnabled(false);
		this.txtField.addActionListener((actionEvent) -> {
			if (txtField.getText().length() > 0) {
					try {
						this.playerName = txtField.getText();
						dOut.writeUTF(this.playerName);
						;
						this.txtField.setText("YOU: " + this.playerName + " ( O )");
						this.txtField.setEnabled(false);

						this.setStatusBarMsg("Alright, here we go! You start first, " + this.playerName + "!");
						this.enableButtons();
					} catch (IOException e) {
						this.setStatusBarMsg("Uh oh!", "I think we lost connection to " + this.opponentName + "...");
					}
				}
			}
		);

		/**	
			Initialize layout
		*/
		super.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(ROW,COLUMN)); // Y,X
		for (int c = 0 ; c < COLUMN ; c++) {
			for (int r = 0 ; r < ROW ; r++) {
				TicTacToe_Button btn = new TicTacToe_Button(c,r);
				btn.addActionListener( (ae) -> {
					try {
						this.dOut.writeInt(btn.getColumn());
						this.dOut.writeInt(btn.getRow());
						btn.setText("O");
						btn.setClickable(false);
						btn.setEnabled(false);
						this.setStatusBarMsg("Waiting for " + this.opponentName + " to move...");
						//this.disableButtons();
						this.waitForOpponent();
					} catch (Exception e) {
						this.setStatusBarMsg("Uh oh!", "I think we lost connection to " + this.opponentName + "...");
					}
				});
				this.buttons[c][r] = btn;
				btn.setEnabled(false);

				mainPanel.add(btn);
			}
		}
		super.add(mainPanel, BorderLayout.CENTER);

		JPanel statusBarPanel = new JPanel();
		statusBarPanel.setLayout(new BorderLayout());
		statusBarPanel.add(this.statusBarSecondary, BorderLayout.NORTH);
		statusBarPanel.add(this.statusBarPrimary, BorderLayout.CENTER);
		statusBarPanel.add(this.txtField, BorderLayout.SOUTH);

		super.add(statusBarPanel, BorderLayout.NORTH);
		/* */
	}

	public void waitForOpponent() {
		try {
			System.out.println("Waiting for opponent...");
			int xPos = dIn.readInt();
			int yPos = dIn.readInt();

			this.buttons[xPos][yPos].setText("X");
			this.buttons[xPos][yPos].setClickable(false);

			this.enableButtons();
		} catch (Exception e) {
			// do nothing
		}
	}

	public int checkWin() {
		// -1: Lose ; 0 = ongoing ; 1 = win ; 2 = full
		return 0;
	}

	public void connectToClient() {
		try {
			this.serverSocket = new ServerSocket(7000);
			this.socket = this.serverSocket.accept(); // waits until an incoming connection is received

			this.dOut = new DataOutputStream(this.socket.getOutputStream());
			this.dIn = new DataInputStream(this.socket.getInputStream());

			String clientAddress = socket.getInetAddress().toString();
			clientAddress = clientAddress.substring(1, clientAddress.length()); // gets rid of '/'
			this.setStatusBarMsg("Oh hey we're connected!", "Finalizing connection with " + clientAddress + " @ " + socket.getLocalPort() + "...");

			this.opponentName = dIn.readUTF(); // waits for client until data is received
			this.txtField.setEnabled(true);
			this.setStatusBarMsg("Connected to " + this.opponentName + "   |   " + clientAddress + " @ " + socket.getLocalPort() + ".", "Type in your name below and hit [ENTER].");
			this.gameStarted = true;
		} catch (Exception e) {
			// Do nothin'...
		}
	}

	private void enableButtons() {
		for (int c = 0 ; c < COLUMN ; c++) {
			for (int r = 0 ; r < ROW ; r++) {
				this.buttons[c][r].setEnabled(this.buttons[c][r].isClickable());
			}
		}
	}
	private void disableButtons() {
		for (int c = 0 ; c < COLUMN ; c++) {
			for (int r = 0 ; r < ROW ; r++) {
				this.buttons[c][r].setEnabled(false);
				this.buttons[c][r].setClickable(false);
			}
		}
	}

	private void setStatusBarMsg(String bottomLabelMsg) {
		this.statusBarPrimary.setText(bottomLabelMsg); // BOTTOM LABEL
	}
	private void setStatusBarMsg(String topLabelMsg, String bottomLabelMsg) {
		this.statusBarPrimary.setText(bottomLabelMsg); // BOTTOM LABEL
		this.statusBarSecondary.setText(topLabelMsg); // TOP LABEL
	}
}