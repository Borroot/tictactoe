package tk.borroot.player.perfect;

import tk.borroot.logic.Board;
import tk.borroot.logic.Logic;
import tk.borroot.player.Player;

import java.util.Random;
import java.util.Vector;

import static java.lang.Math.max;
import static tk.borroot.logic.Symbol.*;

/**
 * This player plays by the negamax algorithm without a limit on the depth,
 * thus making a perfect player. It also makes use of alpha beta pruning.
 * The negamax algorithm is logically exactly the same as the minimax
 * algorithm, but the code is a lot more compact.
 *
 * @author Bram Pulles
 */
public class PlayerNegamax extends Player {

	/**
	 * Give a value to the current state of the board from the perspective of player A.
	 *
	 * @param board which is given a value by the heuristic of winning
	 * @return 1 if this player won, -1 if the other player won and 0 if its a tie
	 */
	private int heuristic(Board board) {
		if (Logic.won(board) != null) {
			return (Logic.won(board) == this.getSymbol()) ? 1 : -1;
		} else {
			return 0;
		}
	}

	/**
	 * The negamax algorithm with alpha beta pruning.
	 *
	 * @param board the board to be valued
	 * @param alpha minimum score the maximizing player is assured of
	 * @param beta  maximum score the minimizing player is assured of
	 * @param color 1 for the max player and -1 for the min player
	 * @return the value of the board for the player
	 */
	private int negamaxAlphaBeta(Board board, int alpha, int beta, int color) {
		if (board.isFull() || Logic.won(board) != null) {
			return color * heuristic(board);
		}

		Vector<Integer> moves = board.moves();
		int value = Integer.MIN_VALUE;
		for (Integer move : moves) {
			// Make the move and do the depth first search.
			board.set(move, ((color == 1) ? this.getSymbol() : (this.getSymbol() == CROSS) ? CIRCLE : CROSS));
			value = max(value, -negamaxAlphaBeta(board, -beta, -alpha, -color));
			board.set(move, EMPTY);

			// Do some alpha beta pruning.
			alpha = max(alpha, value);
			if (alpha >= beta) {
				break;
			}
		}
		return value;
	}

	@Override
	public int move(Board board) {
		int max = Integer.MIN_VALUE;
		Vector<Integer> bestmoves = new Vector<>();

		// Try all the possible moves and take one of the best ones.
		Vector<Integer> moves = board.moves();
		for (Integer move : moves) {
			// Make the move and get the value of how good the move is.
			board.set(move, this.getSymbol());
			int value = -negamaxAlphaBeta(board, Integer.MIN_VALUE + 1, Integer.MAX_VALUE, -1);
			board.set(move, EMPTY);

			// Update the best moves.
			if (value > max) {
				max = value;
				bestmoves.clear();
				bestmoves.add(move);
			} else if (value == max) {
				bestmoves.add(move);
			}
		}

		// Choose a random move from all the equally best moves.
		return bestmoves.get(new Random().nextInt(bestmoves.size()));
	}

	@Override
	public void learn(Player winner) {
		// no reinforcement learning
	}

	@Override
	public String toString() {
		return "Negamax";
	}
}
