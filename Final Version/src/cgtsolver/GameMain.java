package cgtsolver;

public class GameMain
{
  public static void main(String args[])
  {
    AbSolver abSolver = new AbSolver();
    abSolver.search(new GameState(5, 5));
  }
}
