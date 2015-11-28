package basic;

import java.awt.Point;
import java.util.ArrayList;

import lombok.Getter;

public class Game {

	private int width = 4;
	private int height = 4;
	@Getter
	private ArrayList<Point[]> horizontalMoves;
	@Getter
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

		horizontalMoves = new ArrayList<>();
		verticalMoves = new ArrayList<>();

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
		// if true -> horizontal, if false -> vertical
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
		// Let's do this later yo
		/*
		 * if (horizontalMoves.size() == 0) { // verticalPlay } if
		 * (verticalMoves.size() == 0) { // horizontalPlay }
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
			// do children moves for horizontal
			for (int i = 0; i < gameCopy.horizontalMoves.size(); i++) {
				double value = evaluateDH(horizontalMoves.get(i), depth, beta, alpha, player);
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
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > bestValueForChild) {
				bestI = i;
			}
		}
		return horizontalMoves.get(bestI);
	}

	public double evaluateDH(Point[] move, int depth, double alpha, double beta, int player) {
		// TODO
		double heuristicValue = horizontalMoves.size() - verticalMoves.size();
		if (depth == 0 || this.isTerminal()) {
			return player * heuristicValue;
		}
		double bestValue = -Double.MAX_VALUE;
		Game gameCopy = this.copy();
		gameCopy.makeMove(move);
		// TODO: order??
		// player 1 == horizontal, player -1 == vertical
		if (player == 1) {
			// do children moves for horizontal
			for (int i = 0; i < gameCopy.verticalMoves.size(); i++) {
				double value = evaluateDH(verticalMoves.get(i), depth - 1, -beta, -alpha, -player);
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(alpha, bestValue);
				if (alpha >= beta) {
					break;
				}
			}
		} else {
			// do children moves for vertical
			for (int i = 0; i < gameCopy.horizontalMoves.size(); i++) {
				double value = evaluateDH(horizontalMoves.get(i), depth - 1, -beta, -alpha, -player);
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
			// do children moves for horizontal
			for (int i = 0; i < gameCopy.verticalMoves.size(); i++) {
				double value = evaluateDV(verticalMoves.get(i), depth, beta, alpha, player);
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
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > bestValueForChild) {
				bestI = i;
			}
		}
		return verticalMoves.get(bestI);
	}

	public double evaluateDV(Point[] move, int depth, double alpha, double beta, int player) {
		// TODO
		double heuristicValue = verticalMoves.size() - horizontalMoves.size();
		if (depth == 0 || this.isTerminal()) {
			return player * heuristicValue;
		}
		double bestValue = -Double.MAX_VALUE;
		Game gameCopy = this.copy();
		gameCopy.makeMove(move);
		// TODO: order??
		// player 1 == vertical, player -1 == horizontal
		if (player == 1) {
			// do children moves for horizontal
			for (int i = 0; i < gameCopy.horizontalMoves.size(); i++) {
				double value = evaluateDV(horizontalMoves.get(i), depth - 1, -beta, -alpha, -player);
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(alpha, bestValue);
				if (alpha >= beta) {
					break;
				}
			}
		} else {
			// do children moves for vertical
			for (int i = 0; i < gameCopy.verticalMoves.size(); i++) {
				double value = evaluateDV(verticalMoves.get(i), depth - 1, -beta, -alpha, -player);
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
		ArrayList<Point[]> toBeRemoved = new ArrayList<>();

		// go through horizontal moves first and remove any move that is invalid
		// because of the move that has been made
		for (int i = 0; i < horizontalMoves.size(); i++) {
			Point[] move = horizontalMoves.get(i);

			Point point1 = input[0];
			Point point2 = input[1];
			Point point3 = input[2];
			// check if any of the coordinates of the move that we're checking
			// matches with the move that was made
			for (int j = 0; j < move.length; j++) {
				if (move[j] == point1 || move[j] == point2 || move[j] == point3) {
					toBeRemoved.add(move);
				}
			}
		}
		horizontalMoves.removeAll(toBeRemoved);
		toBeRemoved.clear();

		// go through vertical moves and remove any move that is invalid because
		// of the move that has been made
		for (Point[] move : verticalMoves) {
			Point point1 = input[0];
			Point point2 = input[1];
			Point point3 = input[2];
			// check if any of the coordinates of the move that we're checking
			// matches with the move that was made
			for (int j = 0; j < move.length; j++) {
				if (move[j] == point1 || move[j] == point2 || move[j] == point3) {
					toBeRemoved.add(move);
				}
			}
		}
		horizontalMoves.removeAll(toBeRemoved);
	}

	private Game copy() {
		Game game = new Game(true);
		game.horizontalMoves = this.horizontalMoves;
		game.verticalMoves = this.verticalMoves;
		return game;
	}

	// In order to perform floodfill, we convert the move lists to a board.
	private ArrayList<int[][]> findSubgames() {
		// make a new board
		int[][] board = new int[width][height];
		// loop through the lists of moves, any possible moves means that the
		// square is empty -> convert this to a board where 0=full, 1=empty
		for (Point[] ptArray : horizontalMoves) {
			for (Point pt : ptArray) {
				board[pt.x][pt.y] = 1;
			}
		}
		for (Point[] ptArray : verticalMoves) {
			for (Point pt : ptArray) {
				board[pt.x][pt.y] = 1;
			}
		}
		// perform floodfill to identify subgames

		// loop through board
		// once subgame has been made, make new array
		// fill new array with subgame
		// add new array to a list
		// change elements of the initial array to 0, to 'ignore' the cells that
		// were already used

		// copy the board to make sure we do not change anything we do not want
		// to change
		int[][] boardCopy = board.clone();
		// create a new arraylist that will be filled up with the subgames
		ArrayList<int[][]> subGames = new ArrayList<int[][]>();

		// loop through the elements of the board.
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {

				// if the square is empty and not already in another subgame,
				// floodfill from this square
				if (boardCopy[i][j] == 1) {
					// create a new board in floodfill, consisting of this
					// subgame (2), empty but not in the subgame (1), and full
					// (0)
					int[][] subgameBoard = floodFill(boardCopy, new Point(i, j));
					// create a new subgame that we will add in the arraylist of
					// subgames
					int[][] newBoard = new int[height][width];

					// loop through the floodfilled board
					for (int k = 0; k < subgameBoard.length; k++) {
						for (int k2 = 0; k2 < subgameBoard[0].length; k2++) {
							// check if the square is part of the subgame
							if (subgameBoard[k][k2] == 2) {
								// if it is, make the outer loops ignore this
								// square (in order not to get the same subgames
								// multiple times)
								boardCopy[k][k2] = 0;
								// also fill it in the subgame that will be
								// stored in the arraylist
								newBoard[k][k2] = 1;
							}
						}
					}
					// add the subgame to the arraylist
					subGames.add(newBoard);
				}
			}
		}
		return subGames;
	}

	/**
	 * This method identifies subgames by floodfilling the part of the board
	 * that is connected to the given point @param location
	 */
	private int[][] floodFill(int[][] board, Point location) {
		int x = location.x;
		int y = location.y;
		// if this location was empty before, change it to 2, meaning that this
		// square is part of the subgame.
		if (board[x][y] == 1) {
			board[x][y] = 2;
		}
		// floodfill recursively while checking if the point is empty
		if (x < width) {
			Point newPoint = new Point(x + 1, y);
			if (board[newPoint.x][newPoint.y] == 1) {
				floodFill(board, newPoint);
			}
		}
		if (x > 0) {
			Point newPoint = new Point(x - 1, y);
			if (board[newPoint.x][newPoint.y] == 1) {
				floodFill(board, newPoint);
			}
		}
		if (y < height) {
			Point newPoint = new Point(x, y + 1);
			if (board[newPoint.x][newPoint.y] == 1) {
				floodFill(board, newPoint);
			}
		}
		if (y > 0) {
			Point newPoint = new Point(x, y - 1);
			if (board[newPoint.x][newPoint.y] == 1) {
				floodFill(board, newPoint);
			}
		}
		// after the floodfill is done, this will return a board filled with 0,
		// 1 and 2. 0 = taken, 1 = empty but not part of this subgame, 2 = empty
		// and part of this subgame
		return board;
	}
}
