package cgtsolver;

import java.util.List;

public class GameMain
{
  public static void main(String args[])
  {
    AbSolver abSolver = new AbSolver();
    GameState gameState = new GameState(7,7);
    abSolver.search(gameState);
    
//    GameState gameState = new GameState(5, 5);
//    boolean[][] board = gameState.getBoard();
//    board[2][0] = true;
//    board[2][1] = true;
//    board[2][2] = true;
//    board[0][3] = true;
//    board[1][3] = true;
//    board[2][3] = true;
//    int realMovesNumber = gameState.getSafeMovesNumber(false);
//    int a = 3;
  }
}
