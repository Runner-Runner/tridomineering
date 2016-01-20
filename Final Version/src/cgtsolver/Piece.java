package cgtsolver;

import java.awt.Point;

/**
 * Simple container representing a 3-tile piece (vertical or horizontal) to be
 * placed on the board. Therefore this also represents a certain move of a
 * player.
 *
 * @author daniel
 */
public class Piece
{
  public Point p1;
  public Point p2;
  public Point p3;

  /**
   * Keeps a move ordering value representing the value of this move on a
   * certain board.
   */
  private int moveOrderingValue;

  public Piece(Point p1, Point p2, Point p3)
  {
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
  }

  public int getMoveOrderingValue()
  {
    return moveOrderingValue;
  }

  public void setMoveOrderingValue(int moveOrderingValue)
  {
    this.moveOrderingValue = moveOrderingValue;
  }

  @Override
  public String toString()
  {
    return "Piece{" + "p1=" + p1 + ", p2=" + p2 + ", p3=" + p3 + '}';
  }
}
