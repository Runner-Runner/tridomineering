package cgtsolver;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class GameState
{
  private boolean[][] board;
  private int width;
  private int height;
  private List<Piece> moveHistory;
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

  public void doMove(Piece piece)
  {
    board[piece.p1.x][piece.p1.y] = OCCUPIED;
    board[piece.p2.x][piece.p2.y] = OCCUPIED;
    board[piece.p3.x][piece.p3.y] = OCCUPIED;

    moveHistory.add(piece);
  }

  public void undoMove()
  {
    Piece piece = moveHistory.remove(moveHistory.size() - 1);
    board[piece.p1.x][piece.p1.y] = FREE;
    board[piece.p2.x][piece.p2.y] = FREE;
    board[piece.p3.x][piece.p3.y] = FREE;
  }

  public List<Piece> getAvailableMoves()
  {
    return getAvailableMoves(verticalsTurn);
  }
  
  public List<Piece> getAvailableMoves(boolean verticalsTurn)
  {
    //TODO For efficiency, cache value as member until game state changes

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
            i += offset + 1;
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
                new Point(availableMove.p1.y, availableMove.p1.x),
                new Point(availableMove.p2.y, availableMove.p2.x),
                new Point(availableMove.p3.y, availableMove.p3.x));
        realAvailableMoves.add(piece);
      }
      availableMoves = realAvailableMoves;
    }
    return availableMoves;
  }

  public int getSafeMovesAmount()
  {
    int safeMovesAmount = 0;
    List<Piece> ownMoves = getAvailableMoves(verticalsTurn);
    List<Piece> opponentsMoves = getAvailableMoves(!verticalsTurn);
    for (Piece piece : ownMoves)
    {
      boolean overlapFound = false;
      for (Piece opponentsPiece : opponentsMoves)
      {
        if (piece.overlap(opponentsPiece))
        {
          overlapFound = true;
        }
      }
      if (!overlapFound)
      {
        safeMovesAmount++;
      }
    }
    return safeMovesAmount;
  }

  public int getHeuristicValue()
  {
    int ownMoveAmount = getAvailableMoves(verticalsTurn).size();
    int opponentMoveAmount = getAvailableMoves(!verticalsTurn).size();
    int moveDiff = ownMoveAmount - opponentMoveAmount;
    return moveDiff + getSafeMovesAmount();
  }

  public void toggleVerticalsTurn()
  {
    verticalsTurn = !verticalsTurn;
  }
}
