package cgtsolver;

import java.util.List;

public class AbSolver
{
  protected int nodeCounter = 0;
  protected long lastCalcDuration;
  protected Piece optimalMove;

  public static final int POS_INF = 1000000;
  public static final int NEG_INF = -1000000;
  
  public void search(GameState currentGameState)
  {
    long currentTime = System.currentTimeMillis();

    optimalMove = null;

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
    for(int i=0; i<currentGameState.getWidth(); i++)
    {
      for(int j=0; j<currentGameState.getHeight(); j++)
      {
        vboard[i][j] = hboard[i][j] = board[i][j];
      }
    }
    
    
    int scoreVerticalStarts = alphaBetaSearch(gameStateVerticalStarts, 
            Integer.MIN_VALUE, Integer.MAX_VALUE, 49);
    int scoreHorizontalStarts = alphaBetaSearch(gameStateHorizontalStarts, 
            Integer.MIN_VALUE, Integer.MAX_VALUE, 49);
    String result;
    if(scoreVerticalStarts == scoreHorizontalStarts)
    {
      if(scoreVerticalStarts == POS_INF)
      {
        result = "First player win.";
      }
      else
      {
        result = result = "Second player win.";
      }
    }
    else
    {
      if(scoreVerticalStarts == POS_INF)
      {
        result = "Vertical player win.";
      }
      else
      {
        result = "Horizontal player win.";
      }
    }
    
    lastCalcDuration = System.currentTimeMillis() - currentTime;
    double secDuration = ((double) lastCalcDuration) / 1000;

    System.out.println("Result: " + result);
    
    System.out.println("Search Duration: " + secDuration
            + " seconds. Nodes searched: " + nodeCounter + ", Moves: " + currentGameState.getMoveHistory().size());
    
  }

  private int alphaBetaSearch(GameState gameState, int alpha, int beta, int depth)
  {
    //TODO Use cgs for small enough boards
    
    nodeCounter++;

    List<Piece> availableMoves = gameState.getAvailableMoves();
    if (availableMoves.isEmpty())
    {
      return NEG_INF;
    }

    int score = Integer.MIN_VALUE;
    
    int highestHeuristicValue = Integer.MIN_VALUE;
    Piece bestMove = null;
    for (Piece move : availableMoves)
    {
      gameState.doMove(move);
      int heuristicValue = gameState.getHeuristicValue();
      if(heuristicValue > highestHeuristicValue)
      {
        highestHeuristicValue = heuristicValue;
        bestMove = move;
      }
      gameState.undoMove();
    }
    
    gameState.doMove(bestMove);
    gameState.toggleVerticalsTurn();
    
    ///
//    System.out.println("Move: " + bestMove);
    boolean[][] board = gameState.getBoard();
    
    System.out.println("");
    for(int i=0; i<gameState.getHeight(); i++)
    {
      for(int j=0; j<gameState.getWidth(); j++)
      {
        String a = board[j][i] ? "x" : " ";
        System.out.print("|" + a + "|");
      }
      System.out.println("");
    }
    ///
    
    if(depth == 0)
    {
      return gameState.getHeuristicValue();
    }
    int value = -alphaBetaSearch(gameState, -beta, -alpha, --depth);
      
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
      //TODO Pruning still applicable?
//      break;
    }
    
    return score;
  }
}
