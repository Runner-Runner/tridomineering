package cgtsolver;

import java.awt.Point;

public class Piece
{
  public Point p1;
  public Point p2;
  public Point p3;

  public Piece(Point p1, Point p2, Point p3)
  {
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
  }

  public boolean overlap(Piece piece)
  {
    return this.p1.equals(piece.p1) || 
            this.p1.equals(piece.p1) || 
            this.p1.equals(piece.p1);
  }

  @Override
  public String toString()
  {
    return "Piece{" + "p1=" + p1 + ", p2=" + p2 + ", p3=" + p3 + '}';
  }
}
