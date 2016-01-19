package cgtsolver;

import java.security.SecureRandom;
import java.util.HashMap;

public class ZobristTranspositionTable
{
  private int piece;
  private long[] table;
  private int height;
  private int width;

  private HashMap<Long, Integer> transpositionMap;
  
  public ZobristTranspositionTable(int tWidth, int tHeight)
  {
    transpositionMap = new HashMap<>();
    
    piece = 1;
    height = tHeight;
    width = tWidth;
    table = new long[height * width];
    SecureRandom random = new SecureRandom();
    //fill a table of random numbers
    for (int i = 0; i < table.length - 1; i++)
    {
      table[i] = random.nextLong();
    }
  }

  public void clear()
  {
    transpositionMap.clear();
  }
  
  public Integer getHashedValue(long hash)
  {
    return transpositionMap.get(hash);
  }
  
  public void setHashedValue(long hash, int gameValue)
  {
    transpositionMap.put(hash, gameValue);
  }
  
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

  private int[] twoToOne(boolean[][] board)
  {
    int counter = 0;
    int[] boardList = new int[64];
    for (int i = 0; i < width; i++)
    {
      for (int j = 0; j < height; j++)
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
  
  private int[] twoToOne(boolean[][] board, boolean horizontallyFlipped, 
          boolean verticallyFlipped)
  {
    int counter = 0;
    int[] boardList = new int[64];
    
    int hStart = 0;
    int hEnd = width;
    if(horizontallyFlipped)
    {
      hStart = width-1;
      hEnd = -1;
    }
    int hIndex = (int)Math.signum(hEnd - hStart);
    
    for (int i = hStart; i != hEnd; i+=hIndex)
    {
      int vStart = 0;
      int vEnd = height;
      if(verticallyFlipped)
      {
        vStart = height-1;
        vEnd = -1;
      }
      int vIndex = (int)Math.signum(vEnd - vStart);
      
      for (int j = vStart; j != vEnd; j+=vIndex)
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
