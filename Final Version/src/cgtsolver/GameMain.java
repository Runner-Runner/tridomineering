package cgtsolver;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Main class, entry point for starting the solver. The results are printed out
 * inside the solver search.
 *
 * @author daniel
 */
public class GameMain
{
  public static void main(String args[])
  {
    int width = -1;
    int height = -1;
    while (width == -1 || height == -1)
    {
      try
      {
        Scanner in = new Scanner(System.in);
        System.out.print("Enter board width: ");
        width = in.nextInt();
        System.out.print("Enter board height: ");
        height = in.nextInt();
        
        if(width <= 0 || height <= 0)
        {
          width = -1;
          height = -1;
        }
      }
      catch (InputMismatchException ex)
      {
        System.out.println("Invalid user input: Width/Height has to be an integer > 0.");
      }
    }

    System.out.println("Running solver ...");
    AbSolver abSolver = new AbSolver();
    GameState gameState = new GameState(width, height);
    abSolver.search(gameState);
  }
}
