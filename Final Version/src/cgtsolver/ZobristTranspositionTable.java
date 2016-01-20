package cgtsolver;

import java.security.SecureRandom;
import java.util.HashMap;

/**
 * Creates and manages a transposition table working with the Zobrist algorithm.
 *
 * @author daniel
 */
public class ZobristTranspositionTable
{
  private long[] table;
  private int height;
  private int width;

  private HashMap<Long, Integer> transpositionMap;

  public ZobristTranspositionTable(int tWidth, int tHeight)
  {
    transpositionMap = new HashMap<>();

    height = tHeight;
    width = tWidth;
    table = new long[height * width];
    SecureRandom random = new SecureRandom();
    //Fill a table of random numbers
    for (int i = 0; i < table.length - 1; i++)
    {
      table[i] = random.nextLong();
    }
  }

  /**
   * Clear all data from the transposition table.
   */
  public void clear()
  {
    transpositionMap.clear();
  }

  /**
   * Get the game value (win or loss) for a certain position.
   *
   * @param hash
   * @return The game value or null if no known value is stored.
   */
  public Integer getHashedValue(long hash)
  {
    return transpositionMap.get(hash);
  }

  public void setHashedValue(long hash, int gameValue)
  {
    transpositionMap.put(hash, gameValue);
  }

  /**
   * Puts a board state plus its flipped versions (horizontally and/or
   * vertically) in the transposition table along with its game value.
   *
   * @param board
   * @param gameValue
   */
  public void hashAllVariations(boolean[][] board, int gameValue)
  {
    long hash = hash(board, false, false);
    setHashedValue(hash, gameValue);
    hash = hash(board, false, true);
    setHashedValue(hash, gameValue);
    hash = hash(board, true, false);
    setHashedValue(hash, gameValue);
    hash = hash(board, true, true);
    setHashedValue(hash, gameValue);
  }

  /**
   * Creates a hash value for a board.
   *
   * @param board
   * @param horizontallyFlipped
   * @param verticallyFlipped
   * @return
   */
  public long hash(boolean[][] board, boolean horizontallyFlipped,
          boolean verticallyFlipped)
  {
    long h = 0;
    int[] boardList = twoToOne(board, horizontallyFlipped, verticallyFlipped);
    for (int i = 0; i < height * width - 1; i++)
    {
      if (boardList[i] != 0)
      {
        h = h ^ table[i];
      }
    }
    return h;
  }

  /**
   * Converts the boolean board into the appropriate format for hashing.
   *
   * @param board
   * @param horizontallyFlipped
   * @param verticallyFlipped
   * @return
   */
  private int[] twoToOne(boolean[][] board, boolean horizontallyFlipped,
          boolean verticallyFlipped)
  {
    int counter = 0;
    int[] boardList = new int[width * height];

    int hStart = 0;
    int hEnd = width;
    if (horizontallyFlipped)
    {
      hStart = width - 1;
      hEnd = -1;
    }
    int hIndex = (int) Math.signum(hEnd - hStart);

    for (int i = hStart; i != hEnd; i += hIndex)
    {
      int vStart = 0;
      int vEnd = height;
      if (verticallyFlipped)
      {
        vStart = height - 1;
        vEnd = -1;
      }
      int vIndex = (int) Math.signum(vEnd - vStart);

      for (int j = vStart; j != vEnd; j += vIndex)
      {
        if (board[i][j] == true)
        {
          boardList[counter] = 1;
        }
        counter++;
      }
    }
    return boardList;
  }
}
