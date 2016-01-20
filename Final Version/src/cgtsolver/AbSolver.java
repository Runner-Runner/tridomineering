package cgtsolver;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Enhanced Alpha-Beta solver for the game Triomineering. Uses move ordering
 * algorithm and transposition tables (also making use of symmetry).
 *
 * @author daniel
 */
public class AbSolver
{
  /**
   * Counts the number of nodes explored during search. Good indicator for
   * pruning improvements.
   */
  protected int nodeCounter = 0;

  /**
   * Stores the duration of the last search with this solver instance.
   */
  protected long lastCalcDuration;

  /**
   * Transposition table to be used per search on an empty board.
   */
  protected ZobristTranspositionTable zorbistTranspositionTable;

  /**
   * Defines a win for the current player to move.
   */
  public static final int POS_INF = 1000000;

  /**
   * Defines a loss for the current player to move.
   */
  public static final int NEG_INF = -1000000;

  /**
   * Initializes and starts the search (once with vertical starting, once with
   * horizontal starting) and prints out the results (Winner, duration, nodes
   * explored).
   *
   * @param currentGameState The board for which the winner shall be determined.
   */
  public void search(GameState currentGameState)
  {
    zorbistTranspositionTable = new ZobristTranspositionTable(
            currentGameState.getWidth(),
            currentGameState.getHeight());

    long currentTime = System.currentTimeMillis();

    nodeCounter = 0;

    //Create two separate deep copies of the board to be searched.
    GameState gameStateVerticalStarts = new GameState(
            currentGameState.getWidth(),
            currentGameState.getHeight());
    GameState gameStateHorizontalStarts = new GameState(
            currentGameState.getWidth(),
            currentGameState.getHeight());
    gameStateHorizontalStarts.toggleVerticalsTurn();
    boolean[][] vboard = gameStateVerticalStarts.getBoard();
    boolean[][] hboard = gameStateHorizontalStarts.getBoard();
    boolean[][] board = currentGameState.getBoard();
    for (int i = 0; i < currentGameState.getWidth(); i++)
    {
      for (int j = 0; j < currentGameState.getHeight(); j++)
      {
        vboard[i][j] = hboard[i][j] = board[i][j];
      }
    }

    zorbistTranspositionTable.clear();
    int scoreVerticalStarts = alphaBetaSearch(gameStateVerticalStarts,
            NEG_INF, POS_INF);
    zorbistTranspositionTable.clear();
    int scoreHorizontalStarts = alphaBetaSearch(gameStateHorizontalStarts,
            NEG_INF, POS_INF);
    //Interpret and print out the results
    String result;
    if (scoreVerticalStarts == scoreHorizontalStarts)
    {
      if (scoreVerticalStarts == POS_INF)
      {
        result = "First player win.";
      }
      else
      {
        result = "Second player win.";
      }
    }
    else if (scoreVerticalStarts == POS_INF)
    {
      result = "Vertical player win.";
    }
    else
    {
      result = "Horizontal player win.";
    }

    //Measure duration
    lastCalcDuration = System.currentTimeMillis() - currentTime;
    double secDuration = ((double) lastCalcDuration) / 1000;

    System.out.println("Result: " + result);

    System.out.println("Search Duration: " + secDuration
            + " seconds. Nodes searched: " + nodeCounter);
  }

  /**
   * Recursive method to search through the Triomineering game tree.
   *
   * @param gameState
   * @param alpha
   * @param beta
   * @return
   */
  private int alphaBetaSearch(GameState gameState, int alpha, int beta)
  {
    nodeCounter++;

    List<Piece> availableMoves = gameState.getAvailableMoves();
    if (availableMoves.isEmpty())
    {
      //No moves possible? Player to move loses
      return NEG_INF;
    }

    //Check hash of this board state with the transposition table;
    //If result already known, return it.
    long hash = zorbistTranspositionTable.hash(gameState.getBoard(), false, false);
    Integer hashedValue = zorbistTranspositionTable.getHashedValue(hash);
    if (hashedValue != null)
    {
      return hashedValue;
    }

    int realOwn = gameState.getRealMovesNumber(gameState.getVerticalsTurn());
    int realOpp = gameState.getRealMovesNumber(!gameState.getVerticalsTurn());
    int safeOwn = gameState.getSafeMovesNumber(gameState.getVerticalsTurn());
    int safeOpp = gameState.getSafeMovesNumber(!gameState.getVerticalsTurn());

    //Abort criteria
    if (safeOwn > realOpp)
    {
      return POS_INF;
    }
    else if (realOwn < safeOpp)
    {
      return NEG_INF;
    }

    //Set move order evaluation values (delta between real/safe moves before and
    //after the move)
    for (Piece move : availableMoves)
    {
      gameState.doMove(move);

      int nextRealOwn = gameState.getRealMovesNumber(gameState.getVerticalsTurn());
      int nextRealOpp = gameState.getRealMovesNumber(!gameState.getVerticalsTurn());
      int nextSafeOwn = gameState.getSafeMovesNumber(gameState.getVerticalsTurn());
      int nextSafeOpp = gameState.getSafeMovesNumber(!gameState.getVerticalsTurn());

      int moveOrderValue = (realOwn - nextRealOwn) - (realOpp - nextRealOpp)
              + (safeOwn - nextSafeOwn) - (safeOpp - nextSafeOpp);

      move.setMoveOrderingValue(moveOrderValue);

      gameState.undoMove();
    }

    //Put the moves in descending move ordering value order
    Collections.sort(availableMoves, new Comparator<Piece>()
    {
      @Override
      public int compare(Piece o1, Piece o2)
      {
        return o1.getMoveOrderingValue() - o2.getMoveOrderingValue();
      }
    });

    //Traverse through moves in the order of descending move ordering values
    //(Starting with the "best move")
    int score = Integer.MIN_VALUE;
    for (Piece move : availableMoves)
    {
      gameState.doMove(move);
      gameState.toggleVerticalsTurn();

      int value = -alphaBetaSearch(gameState, -beta, -alpha);

      zorbistTranspositionTable.hashAllVariations(gameState.getBoard(), -value);

      gameState.undoMove();
      gameState.toggleVerticalsTurn();

      if (value > score)
      {
        score = value;
      }
      if (score > alpha)
      {
        alpha = score;
      }
      if (alpha >= beta)
      {
        //Pruning
        break;
      }
    }

    return score;
  }
}
