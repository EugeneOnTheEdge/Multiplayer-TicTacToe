import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;

// Networking Library
import java.net.*;
import java.io.*;

public class TicTacToe_ClientComponent extends JComponent {

	private final int ROW = 3;
	private final int COLUMN = 3;

	private TicTacToe_Button buttons[][]; // X,Y
	private String playerName;
	private String opponentName;
	private boolean gameStarted;
	private boolean playerTurn;

	private JLabel statusBarSecondary;
	private JLabel statusBarPrimary;
	private JTextField txtField;

	private Socket socket;
	private DataInputStream dIn;
	private DataOutputStream dOut;
	private String serverIP;
	private int serverPort;

	private final Thread SERVER_LISTENER;
	private final Thread REFRESH_BUTTONS_STATE;

	public TicTacToe_ClientComponent() {
		// Initialize layout and game variables
		this.buttons = new TicTacToe_Button[COLUMN][ROW];
		this.gameStarted = false;
		this.playerTurn = false;
		this.socket = new Socket();
		this.serverIP = null;
		this.opponentName = null;
		this.playerName = null;

		// Thread init
		this.SERVER_LISTENER = new Thread(new ServerProcessingThread());
		this.REFRESH_BUTTONS_STATE = new Thread(new EnableButtonsThread());

		this.statusBarSecondary = new JLabel("Welcome to Multiplayer Tic-Tac-Toe!");
		this.statusBarPrimary = new JLabel("Enter your partner's IP address below and hit [ENTER]");
		this.statusBarPrimary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
		this.statusBarSecondary.setHorizontalAlignment(SwingConstants.CENTER);
		this.statusBarPrimary.setHorizontalAlignment(SwingConstants.CENTER);
		this.txtField = new JTextField();
		this.txtField.setHorizontalAlignment(SwingConstants.CENTER);
		this.txtField.addActionListener(this::onEnterListener);

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
						if (this.playerTurn) {
							this.dOut.writeInt(btn.getColumn());
							this.dOut.writeInt(btn.getRow());
							btn.setText("X");
							btn.setEnabled(false);
							btn.setClickable(false);
							this.setStatusBarMsg("Waiting for " + this.opponentName + " to move...");
							this.disableButtons();
							this.playerTurn = false;

							if (this.checkWin() == 1) {
								this.setStatusBarMsg("You WIN!! " + this.opponentName + " is such a loser hahahah");
							}
							else if (this.checkWin() == 2) {
								this.setStatusBarMsg("It's a DRAW! Well played, you two!");
							}
						}
						else {
							this.setStatusBarMsg("Hold up, " + this.playerName + ", it ain't your turn just yet...");
						}
						
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

	public void connectToServer() {
		try {
			this.socket = new Socket(this.serverIP, this.serverPort);

			this.dOut = new DataOutputStream(this.socket.getOutputStream());
			this.dIn = new DataInputStream(this.socket.getInputStream());

			this.setStatusBarMsg("Oh hey we're connected!", "Enter your name below and hit [ENTER]");
			this.txtField.setText("");
		} catch (Exception e) {
			// Do nothin'...
		}
	}
	private void finalizeServerConnection() {
		try {
			this.playerName = txtField.getText();
			dOut.writeUTF(this.playerName);
							
			this.txtField.setText("YOU: " + this.playerName + " ( X )");
			this.txtField.setEnabled(false);

			String serverAddress = socket.getInetAddress().toString();
			if (serverAddress.charAt(0) == '/') {
				serverAddress = serverAddress.substring(1, serverAddress.length()); // gets rid of '/'
			}

			this.setStatusBarMsg("Finalizing connection with " + serverAddress + "...");
			this.txtField.setEnabled(false);

			this.opponentName = this.dIn.readUTF();
			
			this.setStatusBarMsg("Connected to " + this.opponentName + "   |   " + serverAddress + " @ " + this.serverPort + ".", "Alright! Let's wait for " + this.opponentName + " to move first.");

			this.SERVER_LISTENER.start();
			this.REFRESH_BUTTONS_STATE.start();
		} catch (Exception e) {
			this.setStatusBarMsg("Uh oh!", "We lost connection to " + this.opponentName + "...");
		}
	}


	private int checkWin() {
		// -1: Lose ; 0 = ongoing ; 1 = win ; 2 = full
		int enemyCount = 0;
		int playerCount = 0;
		int checkedButtons = 0;

		// HORIZONTAL CHECKING
		for (int r = 0 ; (r < this.ROW) && ((enemyCount < 3) && (playerCount < 3)) ; r++) {
			enemyCount = 0;
			playerCount = 0;
			for (int c = 0 ; c < this.COLUMN ; c++) {
				if (this.buttons[c][r].getText().equals("X")) {
					playerCount++;
					checkedButtons++;
				}
				else if (this.buttons[c][r].getText().equals("O")) {
					enemyCount++;
					checkedButtons++;
				}
			}
		}

		// VERTICAL CHECKING
		for (int c = 0 ; (c < this.ROW) && ((enemyCount < 3) && (playerCount < 3) && (checkedButtons != this.ROW*this.COLUMN)) ; c++) {
			enemyCount = 0;
			playerCount = 0;
			for (int r = 0 ; r < this.COLUMN ; r++) {
				if (this.buttons[c][r].getText().equals("X")) {
					playerCount++;
				}
				else if (this.buttons[c][r].getText().equals("O")) {
					enemyCount++;
				}
			}
		}

		if (playerCount < 3 && enemyCount < 3) {
			playerCount = 0;
			enemyCount = 0;
		}
		// DIAGONAL CHECKING (top-left to bottom-right)
		for (int i = 0 ; (i < this.ROW) && ((enemyCount < 3) && (playerCount < 3) && (checkedButtons != this.ROW*this.COLUMN)) ; i++) {
			if (this.buttons[i][i].getText().equals("X")) {
				playerCount++;
			}
			else if (this.buttons[i][i].getText().equals("O")) {
				enemyCount++;
			}
		}

		if (playerCount < 3 && enemyCount < 3) {
			playerCount = 0;
			enemyCount = 0;
		}
		// DIAGONAL CHECKING (top-right to bottom-left)
		for (int i = this.COLUMN ; (i > 0) && ((enemyCount < 3) && (playerCount < 3) && (checkedButtons != this.ROW*this.COLUMN)) ; i--) {
			if (this.buttons[i - 1][this.ROW - i].getText().equals("X")) {
				playerCount++;
			}
			else if (this.buttons[i - 1][this.ROW - i].getText().equals("O")) {
				enemyCount++;
			}
		}
		
		if (enemyCount >= 3) {
			this.disableButtons();
			return -1;
		}
		else if (playerCount >= 3) {
			this.disableButtons();
			return 1;
		}
		else if (checkedButtons >= this.ROW*this.COLUMN) {
			return 2;
		}
		return 0;
	}

	public void waitForOpponent() {
		try {
			int xPos = dIn.readInt();
			int yPos = dIn.readInt();

			this.setStatusBarMsg("Your turn, " + this.playerName + "!");

			this.buttons[xPos][yPos].setText("O");
			this.buttons[xPos][yPos].setEnabled(false);
			this.buttons[xPos][yPos].setClickable(false);

			this.playerTurn = true;

			if (this.checkWin() == -1) {
				this.setStatusBarMsg("You LOSE. " + this.opponentName + " is just lucky this time..");
				this.playerTurn = false;
			}
			else if (this.checkWin() == 2) {
				this.setStatusBarMsg("It's a DRAW! Well played, you two!");
				this.playerTurn = false;
			}
		} catch (Exception e) {
			// do nothing
		}
	}

	private void disableButtons() {
		for (int c = 0 ; c < COLUMN ; c++) {
			for (int r = 0 ; r < ROW ; r++) {
				this.buttons[c][r].setEnabled(false);
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

	private void onEnterListener(ActionEvent ae) {
		if (this.txtField.getText().length() > 0) {
			if (this.serverIP == null) {
				this.serverIP = txtField.getText();
				this.txtField.setText("");
				this.setStatusBarMsg("Requesting connection to " + this.serverIP, "Now, enter your partner's port (default: 7000)");
			}
			else {
				if (this.socket.isConnected()) {
					this.finalizeServerConnection();
				}
				else {
					this.serverPort = Integer.parseInt(txtField.getText());
					this.connectToServer();
					if (!this.socket.isConnected()) { // need to check again after every tries to connect to server
						this.txtField.setText("");
						this.setStatusBarMsg("Connection to " + this.serverIP + " failed.", "Please re-enter your partner's IP address...");
						this.serverIP = "";
					}
				}
			}
		}
	}

	public class ServerProcessingThread implements Runnable {
		@Override
		public void run() {
			//System.out.println("Client thread: wait for opponent");
			while (!Thread.interrupted()) {
				waitForOpponent();
			}
		}
	}

	public class EnableButtonsThread implements Runnable {
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				if (playerTurn) {
					for (int c = 0 ; c < COLUMN ; c++) {
						for (int r = 0 ; r < ROW ; r++) {
							if (!playerTurn) {
								disableButtons();
							}
							else {
								if (buttons[c][r].getText().equals("")) {
									buttons[c][r].setEnabled(true);
									try {
										Thread.sleep(50);
									}
									catch (InterruptedException e) {
										// do nothing
									}
								}
							}
						}
					}
				}
			}
		}
	}
}