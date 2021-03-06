package tk.borroot.player.reinforcement.menace;

import tk.borroot.logic.Board;
import tk.borroot.logic.Logic;
import tk.borroot.logic.Transform;
import tk.borroot.player.Player;

import java.util.HashMap;
import java.util.Vector;

import static tk.borroot.logic.Symbol.*;
import static tk.borroot.view.View.askInt;

/**
 * An implementation of MENACE! This player plays
 * the same as MENACE by using (virtual) matchboxes
 * to pick the move.
 *
 * @author Bram Pulles
 */
public class PlayerMenace extends Player {

	/**
	 * The initial amount of beats per box per move. The
	 * amount of beats as a reward for a tie and a win.
	 */
	private final int INIT_BEATS, REWARD_TIE, REWARD_WON;

	/**
	 * This variable represents all the moves menace made during the last game.
	 * The value will be reset at the end of every game.
	 */
	private Vector<Pair<Matchbox, Integer>> moved = new Vector<>();

	/**
	 * This variable represents all the states in menace. So all the matchboxes
	 * with their appropriate board. All the states are made in such a way that
	 * every game was started by player 'X'. This means that when making a move
	 * the board needs to be swapped if the first player was not 'X'.
	 */
	private HashMap<Board, Matchbox> states = new HashMap<>();

	public PlayerMenace() {
		INIT_BEATS = askInt("Enter the initial amount of beats per move per box: ");
		REWARD_TIE = askInt("Enter the reward for a tie: ");
		REWARD_WON = askInt("Enter the reward for a win: ");

		searchStates(new Board(), true);
	}

	/**
	 * Add the board to the states hashmap with a new matchbox if
	 * it is not already in there.
	 *
	 * @param board to be added to the states
	 * @return if the board was already in the states hashmap
	 */
	private boolean processState(Board board) {
		for (Transform transform : Transform.values()) {
			Board trans = Transform.apply(board, transform);
			if (states.containsKey(trans)) {
				return true;
			}
		}
		states.put(board.clone(), new Matchbox(board.moves(), INIT_BEATS));
		return false;
	}

	/**
	 * Search for all the states which can be reached and add these
	 * to the states hashmap. This is done in a depth first search
	 * manner. We also check for all the possible transformations
	 * of the board and only add the unique ones to the states hashmap.
	 *
	 * @param board  to be further explored
	 * @param onturn true means 'X', false means 'O'
	 */
	private void searchStates(Board board, boolean onturn) {
		if (board.isFull() || Logic.won(board) != null) {
			processState(board);
			return;
		}

		Vector<Integer> moves = board.moves();
		if (!processState(board)) {
			for (Integer move : moves) {
				board.set(move, (onturn) ? CROSS : CIRCLE);
				searchStates(board, !onturn);
				board.set(move, EMPTY);
			}
		}
	}

	/**
	 * Check if this player started with this game.
	 *
	 * @param board the board to be checked
	 * @return if this player started the game corresponding to this board
	 */
	private boolean first(Board board) {
		int countThis = 0;
		int countThat = 0;
		for (int i = 0; i < board.size(); i++) {
			if (this.getSymbol() == board.get(i)) {
				countThis++;
			} else if (board.get(i) != EMPTY) {
				countThat++;
			}
		}
		return countThis == countThat;
	}

	/**
	 * Check if the board needs to be swapped such that the
	 * first move was done by a 'X'. This is needed because
	 * the states hashmap is made with 'X' starting.
	 *
	 * @param board to be swapped yes or no
	 * @return if the board needs to be swapped
	 */
	private boolean needsSwap(Board board) {
		if (first(board)) {
			return this.getSymbol() == CIRCLE;
		} else {
			return this.getSymbol() == CROSS;
		}
	}

	/**
	 * Check if menace has died by checking if all of the starting
	 * boards for player 1 or player 2 are empty.
	 *
	 * @return if menace died
	 */
	private boolean menaceDied() {
		// Check the empty board for the first player.
		if (states.get(new Board()).dead()) {
			return true;
		}

		// Check the boards for the second player.
		for (int i : new int[]{0, 1, 4}) {
			Board board = new Board();
			board.set(i, CROSS);
			if (!states.get(board).dead()) {
				// Menace did not die yet.
				return false;
			}
		}

		// All of the boards for the second player are dead.
		return true;
	}

	@Override
	public int move(Board board) throws MoveException, DiedException {
		if (menaceDied()) {
			throw new DiedException("Menace died!");
		}

		// Swap the boards 'X' and 'O' values so the first move was made by an 'X'.
		Board swapped = (needsSwap(board)) ? Transform.swapAll(board) : board.clone();

		// Search for the board in the states hashmap by applying transformations until the board is found.
		for (Transform transform : Transform.values()) {
			Board trans = Transform.apply(swapped, transform);
			if (states.containsKey(trans)) {
				Matchbox matchbox = states.get(trans);
				int transMove = matchbox.move();
				moved.add(new Pair<>(matchbox, transMove));
				return Transform.move(transMove, transform, true);
			}
		}
		return -1;
	}

	@Override
	public void learn(Player winner) {
		final int PUNISHMENT = -1;
		int learn = (winner == null) ? REWARD_TIE : (this.equals(winner) ? REWARD_WON : PUNISHMENT);

		// Remove/add the specified amount of beats for a won/lose/tie.
		for (Pair<Matchbox, Integer> pair : moved) {
			pair.x.add(pair.y, learn);
		}
		moved.clear();
	}

	@Override
	public String toString() {
		return "Menace";
	}
}
