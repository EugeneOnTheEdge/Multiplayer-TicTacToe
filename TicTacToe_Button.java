import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TicTacToe_Button extends JButton {
	private final int ROW_POSITION;
	private final int COL_POSITION;
	private boolean clickable;

	public TicTacToe_Button(int col, int row) { // X,Y
		super();

		this.COL_POSITION = col;
		this.ROW_POSITION = row;
		this.clickable = true;
		super.setFont(new Font("Arial", Font.BOLD, 100));
		super.setVisible(true);
	}

	public int getColumn() {
		return this.COL_POSITION;
	}

	public int getRow() {
		return this.ROW_POSITION;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	public boolean isClickable() {
		return this.clickable;
	}
}