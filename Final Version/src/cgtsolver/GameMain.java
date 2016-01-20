package cgtsolver;

/**
 * Main class, entry point for starting the solver. The results are printed out
 * inside the solver search.
 * @author daniel
 */
public class GameMain
{
  public static void main(String args[])
  {
    AbSolver abSolver = new AbSolver();
    GameState gameState = new GameState(5,5);
    abSolver.search(gameState);
  }
}
