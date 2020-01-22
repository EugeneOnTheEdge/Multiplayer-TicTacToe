import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;

// Networking Library
import java.net.*;
import java.io.*;

public class TicTacToe_ServerComponent_NonBlocking extends JComponent {

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

	private ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dIn;
	private DataOutputStream dOut;
	private final int SERVER_PORT = 7000;

	private final Thread CLIENT_LISTENER;
	private final Thread REFRESH_BUTTONS_STATE;

	public TicTacToe_ServerComponent_NonBlocking() {
		// Initialize layout and game variables
		this.buttons = new TicTacToe_Button[COLUMN][ROW];
		this.gameStarted = false;
		this.playerTurn = true;

		// Thread init
		this.CLIENT_LISTENER = new Thread(new ServerProcessingThread());
		this.REFRESH_BUTTONS_STATE = new Thread(new EnableButtonsThread());

		this.statusBarSecondary = new JLabel("Welcome to Multiplayer Tic-Tac-Toe by /EugeneOnTheEdge!");
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

						this.CLIENT_LISTENER.start();
						this.REFRESH_BUTTONS_STATE.start();
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
						if (this.playerTurn) {
							this.dOut.writeInt(btn.getColumn());
							this.dOut.writeInt(btn.getRow());
							btn.setText("O");
							btn.setClickable(false);
							btn.setEnabled(false);
							this.setStatusBarMsg("Waiting for " + this.opponentName + " to move...");
							this.disableButtons();
							this.playerTurn = false;
							if (this.checkWin() == 1) {
								this.setStatusBarMsg("You WIN!! " + this.opponentName + " is such a loser hahahah");
								this.playerTurn = false;
							}
							else if (this.checkWin() == 2) {
								this.setStatusBarMsg("It's a DRAW! Well played, you two!");
								this.playerTurn = false;
							}
						}
						else {
							this.setStatusBarMsg("Hold up, " + this.playerName + ", it ain't your turn just yet...");
						}
						
					} catch (Exception e) {
						System.out.println(e);
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
				if (this.buttons[c][r].getText().equals("O")) {
					playerCount++;
					checkedButtons++;
				}
				else if (this.buttons[c][r].getText().equals("X")) {
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
				if (this.buttons[c][r].getText().equals("O")) {
					playerCount++;
				}
				else if (this.buttons[c][r].getText().equals("X")) {
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
			if (this.buttons[i][i].getText().equals("O")) {
				playerCount++;
			}
			else if (this.buttons[i][i].getText().equals("X")) {
				enemyCount++;
			}
		}

		if (playerCount < 3 && enemyCount < 3) {
			playerCount = 0;
			enemyCount = 0;
		}
		// DIAGONAL CHECKING (top-right to bottom-left)
		for (int i = this.COLUMN ; (i > 0) && ((enemyCount < 3) && (playerCount < 3) && (checkedButtons != this.ROW*this.COLUMN)) ; i--) {
			if (this.buttons[i - 1][this.ROW - i].getText().equals("O")) {
				playerCount++;
			}
			else if (this.buttons[i - 1][this.ROW - i].getText().equals("X")) {
				enemyCount++;
			}
		}
		
		if (enemyCount >= 3) {
			return -1;
		}
		else if (playerCount >= 3) {
			return 1;
		}
		else if (checkedButtons >= this.ROW*this.COLUMN) {
			this.playerTurn = false;
			return 2;
		}
		return 0;
	}

	public void connectToClient() {
		try {
			this.serverSocket = new ServerSocket(this.SERVER_PORT);
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

	private void disableButtons() {
		for (int c = 0 ; c < this.COLUMN ; c++) {
			for (int r = 0 ; r < this.ROW ; r++) {
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

	public void waitForOpponent() {
		try {
			int xPos = dIn.readInt();
			int yPos = dIn.readInt();

			this.buttons[xPos][yPos].setText("X");
			this.buttons[xPos][yPos].setClickable(false);
			this.playerTurn = true;

			this.setStatusBarMsg("Your turn, " + this.playerName + "!");

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

	public class ServerProcessingThread implements Runnable {
		@Override
		public void run() {
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
								buttons[c][r].setEnabled(false);
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