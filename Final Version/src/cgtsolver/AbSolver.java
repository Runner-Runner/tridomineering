package cgtsolver;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AbSolver
{
  protected int nodeCounter = 0;
  protected long lastCalcDuration;
  protected ZobristTranspositionTable zorbist;
  
  public static final int POS_INF = 1000000;
  public static final int NEG_INF = -1000000;

  public void search(GameState currentGameState)
  {
    zorbist = new ZobristTranspositionTable(currentGameState.getWidth(), 
            currentGameState.getHeight());
    
    long currentTime = System.currentTimeMillis();

    nodeCounter = 0;

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

    zorbist.clear();
    int scoreVerticalStarts = alphaBetaSearch(gameStateVerticalStarts,
            NEG_INF, POS_INF);
    zorbist.clear();
    int scoreHorizontalStarts = alphaBetaSearch(gameStateHorizontalStarts,
            NEG_INF, POS_INF);
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

    lastCalcDuration = System.currentTimeMillis() - currentTime;
    double secDuration = ((double) lastCalcDuration) / 1000;

    System.out.println("Result: " + result);

    System.out.println("Search Duration: " + secDuration
            + " seconds. Nodes searched: " + nodeCounter);
  }

  private int alphaBetaSearch(GameState gameState, int alpha, int beta)
  {
    //TODO Use cgs for small enough boards

    nodeCounter++;

    List<Piece> availableMoves = gameState.getAvailableMoves();
    if (availableMoves.isEmpty())
    {
      return NEG_INF;
    }
    
    long hash = zorbist.hash(gameState.getBoard(), false, false);
    Integer hashedValue = zorbist.getHashedValue(hash);
    if(hashedValue != null)
    {
      return hashedValue;
    }
    
    int realOwn = gameState.getRealMovesNumber(gameState.getVerticalsTurn());
    int realOpp = gameState.getRealMovesNumber(!gameState.getVerticalsTurn());
    int safeOwn = gameState.getSafeMovesNumber(gameState.getVerticalsTurn());
    int safeOpp = gameState.getSafeMovesNumber(!gameState.getVerticalsTurn());

    //Abort criterion
    if(safeOwn > realOpp)
    {
      return POS_INF;
    }
    else if(realOwn < safeOpp)
    {
      return NEG_INF;
    }
    
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

    Collections.sort(availableMoves, new Comparator<Piece>()
    {
      @Override
      public int compare(Piece o1, Piece o2)
      {
        return o1.getMoveOrderingValue() - o2.getMoveOrderingValue();
      }
    });

    //Traverse through move according
    int score = Integer.MIN_VALUE;
    for (Piece move : availableMoves)
    {
      gameState.doMove(move);
      gameState.toggleVerticalsTurn();
      
      int value = -alphaBetaSearch(gameState, -beta, -alpha);

      zorbist.hashAllVariations(gameState.getBoard(), -value);
      
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
        break;
      }
    }
    
    ///
//    System.out.println("Move: " + bestMove);
//    boolean[][] board = gameState.getBoard();
//    
//    System.out.println("");
//    for(int i=0; i<gameState.getHeight(); i++)
//    {
//      for(int j=0; j<gameState.getWidth(); j++)
//      {
//        String a = board[j][i] ? "x" : " ";
//        System.out.print("|" + a + "|");
//      }
//      System.out.println("");
//    }
    ///

    return score;
  }
}
