package cgtsolver;

public class GameMain
{
  public static void main(String args[])
  {
    AbSolver abSolver = new AbSolver();
    abSolver.search(new GameState(7,3));

//    GameState gameState = new GameState(6, 6);
//    boolean[][] board = gameState.getBoard();
//    board[0][0] = true;
//    board[1][0] = true;
//    board[2][0] = true;
//    List<Piece> availableMoves = gameState.getAvailableMoves();
//    int a = 3;
  }
}
