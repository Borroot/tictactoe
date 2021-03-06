package tk.borroot.player;

import tk.borroot.logic.Board;
import tk.borroot.logic.Symbol;
import tk.borroot.player.reinforcement.menace.DiedException;
import tk.borroot.player.reinforcement.menace.MoveException;

/**
 * This class represents a player with a symbol.
 * All other players implement this class and the move function.
 *
 * @author Bram Pulles
 */
public abstract class Player {

	private Symbol symbol;

	public Player() {
	}

	/**
	 * This function will be asked to players when they can make a move.
	 *
	 * @param board to be made a move on
	 * @return a value in [0..8] which corresponds to the move
	 * @throws MoveException if the player cannot make a move anymore
	 * @throws DiedException if the player died
	 */
	public abstract int move(Board board) throws MoveException, DiedException;

	/**
	 * This function will be called at the end of each game. This function
	 * should activate the learning mechanism of the reinforcement learning algorithms.
	 *
	 * @param winner who won the last game (with this board)
	 */
	public abstract void learn(Player winner);

	/**
	 * Set the symbol of this player.
	 *
	 * @param symbol new symbol
	 */
	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

	/**
	 * Get the symbol of this player.
	 *
	 * @return players symbol
	 */
	public Symbol getSymbol() {
		return symbol;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Player) {
			return symbol == ((Player) o).getSymbol();
		}
		return false;
	}

	@Override
	public String toString() {
		return "Please give this player a unique name!";
	}
}
