package cgtsolver;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game state on a Triomineering board. Stores the size, occupied
 * tiles, player to move and the move history so far. Enables making and undoing
 * moves, as well as getting the number of remaining real and safe moves for
 * either player.
 *
 * @author daniel
 */
public class GameState
{
  private boolean[][] board;
  private int width;
  private int height;

  /**
   * List of the moves made so far by both players.
   */
  private List<Piece> moveHistory;

  /**
   * Stores which player is to move.
   */
  private boolean verticalsTurn = true;

  public static final boolean FREE = false;
  public static final boolean OCCUPIED = true;

  public GameState(int width, int height)
  {
    this.width = width;
    this.height = height;
    board = new boolean[width][height];
    moveHistory = new ArrayList<>();
  }

  /**
   * Occupies the tiles of the set piece on the board and stores the move in
   * history.
   *
   * @param piece
   */
  public void doMove(Piece piece)
  {
    board[piece.p1.x][piece.p1.y] = OCCUPIED;
    board[piece.p2.x][piece.p2.y] = OCCUPIED;
    board[piece.p3.x][piece.p3.y] = OCCUPIED;

    moveHistory.add(piece);
  }

  /**
   * Undo the last move that was made on the board.
   */
  public void undoMove()
  {
    Piece piece = moveHistory.remove(moveHistory.size() - 1);
    board[piece.p1.x][piece.p1.y] = FREE;
    board[piece.p2.x][piece.p2.y] = FREE;
    board[piece.p3.x][piece.p3.y] = FREE;
  }

  /**
   * Gets the available moves for the player to move. Convenience method.
   *
   * @return
   */
  public List<Piece> getAvailableMoves()
  {
    return getAvailableMoves(verticalsTurn);
  }

  /**
   * Gets the available moves for a certain player.
   *
   * @param verticalsTurn
   * @return
   */
  public List<Piece> getAvailableMoves(boolean verticalsTurn)
  {
    List<Piece> availableMoves = new ArrayList<>();

    boolean[][] localTiles = board;

    if (verticalsTurn)
    {
      //Just temporarely rotate the game for vertical player (turn 90° cw)
      localTiles = new boolean[height][width];

      for (int i = 0; i < width; i++)
      {
        for (int j = 0; j < height; j++)
        {
          localTiles[height - 1 - j][i] = board[i][j];
        }
      }
    }

    for (int j = 0; j < localTiles[0].length; j++)
    {
      widthloop:
      for (int i = 0; i < localTiles.length; i++)
      {
        if (i + 2 >= localTiles.length)
        {
          continue;
        }

        for (int offset = 2; offset >= 0; offset--)
        {
          if (localTiles[i + offset][j])
          {
            //Skip behind the occupied tile
            i += offset;
            continue widthloop;
          }
        }
        Piece piece = new Piece(new Point(i, j), new Point(i + 1, j),
                new Point(i + 2, j));
        availableMoves.add(piece);
      }
    }

    if (verticalsTurn)
    {
      // Rotate back indices for vertical player
      List<Piece> realAvailableMoves = new ArrayList<>();
      for (Piece availableMove : availableMoves)
      {
        Piece piece = new Piece(
                new Point(availableMove.p1.y, height - 1 - availableMove.p1.x),
                new Point(availableMove.p2.y, height - 1 - availableMove.p2.x),
                new Point(availableMove.p3.y, height - 1 - availableMove.p3.x));
        realAvailableMoves.add(piece);
      }
      availableMoves = realAvailableMoves;
    }
    return availableMoves;
  }

  /**
   * Returns the number of real moves: the maximum number of moves a player can
   * make if the other would not move at all. This defines an upper boundary.
   *
   * @param verticalsTurn
   * @return
   */
  public int getRealMovesNumber(boolean verticalsTurn)
  {
    int realMovesNumber = 0;

    boolean[][] localTiles = board;

    if (verticalsTurn)
    {
      //Just temporarely rotate the game for vertical player (turn 90° cw)
      localTiles = new boolean[height][width];

      for (int i = 0; i < width; i++)
      {
        for (int j = 0; j < height; j++)
        {
          localTiles[height - 1 - j][i] = board[i][j];
        }
      }
    }

    for (int j = 0; j < localTiles[0].length; j++)
    {
      widthloop:
      for (int i = 0; i < localTiles.length; i++)
      {
        if (i + 2 >= localTiles.length)
        {
          continue;
        }

        for (int offset = 2; offset >= 0; offset--)
        {
          if (localTiles[i + offset][j])
          {
            //Skip behind the occupied tile
            i += offset;
            continue widthloop;
          }
        }
        realMovesNumber++;
        i += 2;
      }
    }
    return realMovesNumber;
  }

  /**
   * Returns the number of safe moves: the number of moves a player can make if
   * that can not be prevented by any move of the opponent. This defines an
   * lower boundary.
   *
   * @param verticalsTurn
   * @return
   */
  public int getSafeMovesNumber(boolean verticalsTurn)
  {
    int safeMovesNumber = 0;

    boolean[][] localTiles = new boolean[width][height];
    //Create deep copy
    for (int i = 0; i < width; i++)
    {
      System.arraycopy(board[i], 0, localTiles[i], 0, height);
    }

    //Occupy all tiles where the opponent could move.
    List<Piece> opponentsMoves = getAvailableMoves(!verticalsTurn);
    for (Piece piece : opponentsMoves)
    {
      localTiles[piece.p1.x][piece.p1.y] = OCCUPIED;
      localTiles[piece.p2.x][piece.p2.y] = OCCUPIED;
      localTiles[piece.p3.x][piece.p3.y] = OCCUPIED;
    }

    //Then, just get the maximum numbers of moves possible for this player
    
    if (verticalsTurn)
    {
      //Just temporarely rotate the game for vertical player (turn 90° cw)
      boolean[][] rotatedLocalTiles = new boolean[height][width];

      for (int i = 0; i < width; i++)
      {
        for (int j = 0; j < height; j++)
        {
          rotatedLocalTiles[height - 1 - j][i] = localTiles[i][j];
        }
      }
      localTiles = rotatedLocalTiles;
    }

    for (int j = 0; j < localTiles[0].length; j++)
    {
      widthloop:
      for (int i = 0; i < localTiles.length; i++)
      {
        if (i + 2 >= localTiles.length)
        {
          continue;
        }

        for (int offset = 2; offset >= 0; offset--)
        {
          if (localTiles[i + offset][j])
          {
            //Skip behind the occupied tile
            i += offset;
            continue widthloop;
          }
        }
        safeMovesNumber++;
        i += 2;
      }
    }
    return safeMovesNumber;
  }

  /**
   * Switches the player to move.
   */
  public void toggleVerticalsTurn()
  {
    verticalsTurn = !verticalsTurn;
  }

  public List<Piece> getMoveHistory()
  {
    return moveHistory;
  }

  public boolean[][] getBoard()
  {
    return board;
  }

  public int getWidth()
  {
    return width;
  }

  public int getHeight()
  {
    return height;
  }

  public boolean getVerticalsTurn()
  {
    return verticalsTurn;
  }
}
