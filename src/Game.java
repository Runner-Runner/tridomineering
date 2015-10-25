import java.awt.Point;
import java.util.ArrayList;

public class Game {

	private int width = 4;
	private int height = 4;
	private ArrayList<Point[]> horizontalMoves;
	private ArrayList<Point[]> verticalMoves;
	public static final int MAX_DEPTH = 100;

	public Game() {

		
		// initialize DONE

		// create initial moveLists DONE

		// play game :
		// evaluate
		// player takes move
		// move that have been invalidated are removed from enemy moveList DONE
		// switch turn
		// check if game is over

		////////////////////////////////////////////

		// initialize horizontal player's moves
		for (int i = 0; i < width - 2; i++) {
			for (int j = 0; j < height; j++) {
				Point[] pointArray = new Point[] { new Point(i, j), new Point(i + 1, j), new Point(i + 2, j) };
				horizontalMoves.add(pointArray);
			}
		}

		// initialize vertical player's moves
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height - 2; j++) {
				Point[] pointArray = new Point[] { new Point(i, j), new Point(i, j + 1), new Point(i, j + 2) };
				verticalMoves.add(pointArray);
			}
		}
		//if true -> horizontal, if false -> vertical
		boolean turn = true;
		while (horizontalMoves.size() > 0 && verticalMoves.size() > 0) {
			if (turn) {
				this.evaluatePH(MAX_DEPTH);
			} else {
				this.evaluatePV(MAX_DEPTH);
			}
			turn = !turn;
		}

		if (turn) {
			System.out.println("The loser is Horizontal!!!!!!!!!");
		}
		if (!turn) {
			System.out.println("The loser is Vertical!!!!!!!!!");
		}
		//Let's do this later yo
	/*	if (horizontalMoves.size() == 0) {
			// verticalPlay
		}
		if (verticalMoves.size() == 0) {
			// horizontalPlay
		}
*/
	}

	public Game(boolean copy) {
		// empty
	}

	public Point[] evaluatePH(int depth) {
		double alpha = -Double.MAX_VALUE;
		double beta = Double.MAX_VALUE;
		int player = 1;
		// TODO
		Point[] bestMove = null;
		if (depth == 0 || this.isTerminal()) {
			System.out.println("The starting state is terminal questionmark?");
			return bestMove;
		}
		double bestValue = -Double.MAX_VALUE;
		Game gameCopy = this.copy();
		double[] scores = new double[horizontalMoves.size()];
		if (player == 1) {
			//do children moves for horizontal
			for (int i = 0; i < gameCopy.horizontalMoves.size(); i++) {
				double value = evaluateDH(horizontalMoves.get(i), depth , beta, alpha, player);
				scores[i] = value;
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(alpha, bestValue);
				if (alpha >= beta) {
					break;
				}
			}
		}
		int bestI = 0;
		double bestValueForChild = -Double.MAX_VALUE;
		for (int i=0; i<scores.length; i++) {
			if (scores[i] > bestValueForChild) {
				bestI= i;
			}
		}
		return horizontalMoves.get(bestI);
	}

	public double evaluateDH(Point[] move, int depth, double alpha, double beta, int player) {
		// TODO
		double heuristicValue = horizontalMoves.size() - verticalMoves.size();
		if (depth == 0 || this.isTerminal()) {
			return player*heuristicValue;
		}
		double bestValue = -Double.MAX_VALUE;
		Game gameCopy = this.copy();
		gameCopy.makeMove(move);
		// TODO: order??
		// player 1 == horizontal, player -1 == vertical
		if (player == 1) {
			//do children moves for horizontal
			for (int i = 0; i < gameCopy.verticalMoves.size(); i++) {
				double value = evaluateDH(verticalMoves.get(i), depth -1, -beta, -alpha, -player);
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(alpha, bestValue);
				if (alpha >= beta) {
					break;
				}
			}
		} else {
			//do children moves for vertical
			for (int i = 0; i < gameCopy.horizontalMoves.size(); i++) {
				double value = evaluateDH(horizontalMoves.get(i), depth -1, -beta, -alpha, -player);
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(alpha, bestValue);
				if (alpha >= beta) {
					break;
				}
			}
		}
		return bestValue;
	}
	
	public Point[] evaluatePV(int depth) {
		double alpha = -Double.MAX_VALUE;
		double beta = Double.MAX_VALUE;
		int player = 1;
		// TODO
		Point[] bestMove = null;
		if (depth == 0 || this.isTerminal()) {
			System.out.println("The starting state is terminal questionmark?");
			return bestMove;
		}
		double bestValue = -Double.MAX_VALUE;
		Game gameCopy = this.copy();
		double[] scores = new double[verticalMoves.size()];
		if (player == 1) {
			//do children moves for horizontal
			for (int i = 0; i < gameCopy.verticalMoves.size(); i++) {
				double value = evaluateDV(verticalMoves.get(i), depth , beta, alpha, player);
				scores[i] = value;
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(alpha, bestValue);
				if (alpha >= beta) {
					break;
				}
			}
		}
		int bestI = 0;
		double bestValueForChild = -Double.MAX_VALUE;
		for (int i=0; i<scores.length; i++) {
			if (scores[i] > bestValueForChild) {
				bestI= i;
			}
		}
		return verticalMoves.get(bestI);
	}

	public double evaluateDV(Point[] move, int depth, double alpha, double beta, int player) {
		// TODO
		double heuristicValue = verticalMoves.size() - horizontalMoves.size();
		if (depth == 0 || this.isTerminal()) {
			return player*heuristicValue;
		}
		double bestValue = -Double.MAX_VALUE;
		Game gameCopy = this.copy();
		gameCopy.makeMove(move);
		// TODO: order??
		// player 1 == vertical, player -1 == horizontal
		if (player == 1) {
			//do children moves for horizontal
			for (int i = 0; i < gameCopy.horizontalMoves.size(); i++) {
				double value = evaluateDV(horizontalMoves.get(i), depth -1, -beta, -alpha, -player);
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(alpha, bestValue);
				if (alpha >= beta) {
					break;
				}
			}
		} else {
			//do children moves for vertical
			for (int i = 0; i < gameCopy.verticalMoves.size(); i++) {
				double value = evaluateDV(verticalMoves.get(i), depth -1, -beta, -alpha, -player);
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(alpha, bestValue);
				if (alpha >= beta) {
					break;
				}
			}
		}
		return bestValue;
	}

	public boolean isTerminal() {
		return (horizontalMoves.isEmpty() || verticalMoves.isEmpty());

	}

	public void makeMove(Point[] input) {
		// go through horizontal moves first and remove any move that is invalid
		// because of the move that has been made
		for (Point[] move : horizontalMoves) {
			Point point1 = input[1];
			Point point2 = input[2];
			Point point3 = input[3];
			// check if any of the coordinates of the move that we're checking
			// matches with the move that was made
			for (int i = 0; i < move.length; i++) {
				if (move[i] == point1) {
					horizontalMoves.remove(move);
				} else if (move[i] == point2) {
					horizontalMoves.remove(move);
				} else if (move[i] == point3) {
					horizontalMoves.remove(move);
				}
			}

		}
		// go through vertical moves and remove any move that is invalid because
		// of the mvoe that has been made
		for (Point[] move : verticalMoves) {
			Point point1 = input[1];
			Point point2 = input[2];
			Point point3 = input[3];
			// check if any of the coordinates of the move that we're checking
			// matches with the move that was made
			for (int i = 0; i < move.length; i++) {
				if (move[i] == point1) {
					horizontalMoves.remove(move);
				} else if (move[i] == point2) {
					horizontalMoves.remove(move);
				} else if (move[i] == point3) {
					horizontalMoves.remove(move);
				}
			}

		}

	}

	private Game copy() {
		Game game = new Game(true);
		game.horizontalMoves = this.horizontalMoves;
		game.verticalMoves = this.verticalMoves;
		return game;
	}
}
