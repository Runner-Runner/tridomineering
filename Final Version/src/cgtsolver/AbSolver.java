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

    int score = alphaBetaSearch(currentGameState, Integer.MIN_VALUE, 
            Integer.MAX_VALUE, true);

    lastCalcDuration = System.currentTimeMillis() - currentTime;
    double secDuration = ((double) lastCalcDuration) / 1000;

    System.out.println("Search Duration: " + secDuration
            + " seconds. Nodes searched: " + nodeCounter);
  }
//TODO remove flag
  private int alphaBetaSearch(GameState gameState, int alpha, int beta, 
          boolean startFlag)
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
    
    System.out.println("Move: " + bestMove);
    
    int value = -alphaBetaSearch(gameState, -beta, -alpha, false);
      
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
      //TODO Pruning still applicable?
//      break;
    }
    
    return score;
  }
}
