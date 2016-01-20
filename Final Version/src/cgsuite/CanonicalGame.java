/*
 * CanonicalGame.java
 *
 * Created on October 15, 2002, 4:53 PM
 * $Id: CanonicalGame.java,v 1.18 2003/12/12 20:38:57 asiegel Exp $
 */

/* ****************************************************************************

 Combinatorial Game Suite - A program to analyze combinatorial games
 Copyright (C) 2003  Aaron Siegel (asiegel@users.sourceforge.net)

 Combinatorial Game Suite is free software; you can redistribute it
 and/or modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2 of the
 License, or (at your option) any later version.

 Combinatorial Game Suite is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Combinatorial Game Suite; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 **************************************************************************** */

package cgsuite;

import java.lang.*;
import java.util.*;
import java.io.Serializable;

import nl.unimaas.dke.domineering.grid.AbstractGrid;

/**
 * A short combinatorial game in canonical form. Every option of a
 * <code>CanonicalGame</code> is again a <code>CanonicalGame</code>. In
 * addition, it is guaranteed that:
 * <ul>
 * <li>There are no dominated options. That is, if G and H are left options of
 * the same <code>CanonicalGame</code>, it is guaranteed that G is not less than
 * or equal to H, and likewise for right options.
 * <li>There are no reversible options. That is, if H is a left option of a
 * <code>CanonicalGame</code> G, then no right option of H is less than or equal
 * to G. Likewise, if H is a right option of G, then no left option of H is
 * greater than or equal to G.
 * </ul>
 * The <code>CanonicalGame</code> class guarantees that at most one copy of each
 * <code>CanonicalGame</code> resides in memory at any given time. This is a
 * global constraint that permits a large number of space and time
 * optimizations. In order to enforce this constraint,
 * <code>CanonicalGame</code> contains no public constructors. To construct a
 * <code>CanonicalGame</code>, use one of the <code>from*</code> methods. The
 * <code>from*</code> method will return a reference to the specified object, if
 * it already exists; otherwise it will privately construct a new one.
 * 
 * @author Aaron Siegel
 * @version $Revision: 1.18 $ $Date: 2003/12/12 20:38:57 $
 */
public final class CanonicalGame implements StopperGame, Comparable
{
   // //////////////////////////////////////////////////////////////////////
   // Nested classes.

   // Instances of this class are used as hashtable keys for the
   // number-up-star cache.
   private final static class NusKey implements Serializable
   {
      public DyadicRational number;
      public int            upMultiple;
      public int            nimber;

      public NusKey()
      {
      }

      public NusKey(DyadicRational number, int upMultiple, int nimber)
      {
         this.number = number;
         this.upMultiple = upMultiple;
         this.nimber = nimber;
      }

      public boolean equals(Object obj)
      {
         if (obj == null || !(obj instanceof NusKey))
         {
            return false;
         }
         NusKey nk = (NusKey) obj;
         return number.equals(nk.number) && upMultiple == nk.upMultiple && nimber == nk.nimber;
      }

      public int hashCode()
      {
         return 1023 * (upMultiple + 1023 * nimber) + number.hashCode();
      }
   }

   // Instances of this class are used as hashtable keys for the
   // game cache, which maps canonical (left and right) option lists to the
   // canonical form of the associated game.
   private final static class OptionsKey implements Serializable
   {
      public CanonicalGame[] leftOptions;
      public CanonicalGame[] rightOptions;

      public OptionsKey(CanonicalGame[] leftOptions, CanonicalGame[] rightOptions)
      {
         this.leftOptions = leftOptions;
         this.rightOptions = rightOptions;
      }

      public boolean equals(Object obj)
      {
         if (obj == null || !(obj instanceof OptionsKey))
         {
            return false;
         }
         OptionsKey ok = (OptionsKey) obj;
         if (leftOptions.length != ok.leftOptions.length || rightOptions.length != ok.rightOptions.length)
         {
            return false;
         }
         for (int i = 0; i < leftOptions.length; i++)
         {
            if (!leftOptions[i].equals(ok.leftOptions[i]))
            {
               return false;
            }
         }
         for (int i = 0; i < rightOptions.length; i++)
         {
            if (!rightOptions[i].equals(ok.rightOptions[i]))
            {
               return false;
            }
         }
         return true;
      }

      public int hashCode()
      {
         int hashCode = 1;
         for (int i = 0; i < leftOptions.length; i++)
         {
            hashCode = 31 * hashCode + leftOptions[i].hashCode();
         }
         for (int i = 0; i < rightOptions.length; i++)
         {
            hashCode = 31 * hashCode + rightOptions[i].hashCode();
         }
         return hashCode;
      }
   }

   // Instances of this class are used as hashtable keys for the operation
   // cache. Currently only sums are cached; support for other operations
   // might be added in the future.
   private final static class OperationKey implements Serializable
   {
      public final static int OPERATION_TYPE_SUM = 0, OPERATION_TYPE_NORTON_PRODUCT = 1, OPERATION_TYPE_ATOMIC_WEIGHT = 2, OPERATION_TYPE_CONWAY_PRODUCT = 3;

      public int              operationType;
      public CanonicalGame    g, h;

      public OperationKey(int initOperationType, CanonicalGame initG, CanonicalGame initH)
      {
         operationType = initOperationType;
         g = initG;
         h = initH;
      }

      public boolean equals(Object obj)
      {
         if (obj == null || !(obj instanceof OperationKey))
         {
            return false;
         }
         OperationKey ok = (OperationKey) obj;
         return operationType == ok.operationType && ((g == ok.g && h == ok.h) || (operationType == OPERATION_TYPE_SUM && g == ok.h && h == ok.g));
         // (Sum is commutative!)
      }

      public int hashCode()
      {
         return operationType + (g == null ? 0 : g.hashCode()) + (h == null ? 0 : h.hashCode());
      }
   }

   // //////////////////////////////////////////////////////////////////////
   // Private static data (caches, etc.)

   // The following three caches are maintained:
   // nusCache maps a number-up-star combination to the corresponding
   // CanonicalGame.
   // gameCache maps left and right option lists to the corresponding
   // CanonicalGame.
   private static Map          nusCache, gameCache;

   private static List         idCatalog;
   private static int          nextID;

   static
   {
      nusCache = new HashMap(127);
      gameCache = new HashMap(2047);
      idCatalog = new ArrayList(2047);
      nextID = 0;
   }

   // //////////////////////////////////////////////////////////////////////
   // Constants.

   /**
    * A static reference to the game 0.
    */
   public static CanonicalGame ZERO;
   /**
    * A static reference to the game *.
    */
   public static CanonicalGame STAR;

   private static CanonicalGame TWO, MINUS_TWO, UP, UP_STAR;

   static
   {
      ZERO = new CanonicalGame();
      ZERO.leftOptions = ZERO.rightOptions = new CanonicalGame[0];
      ZERO.nusKey = new NusKey(new DyadicRational(0, 1), 0, 0);
      nusCache.put(ZERO.nusKey, ZERO);
      gameCache.put(new OptionsKey(ZERO.leftOptions, ZERO.rightOptions), ZERO);

      STAR = fromNumberUpStar(DyadicRational.ZERO, 0, 1);
      UP = fromNumberUpStar(DyadicRational.ZERO, 1, 0);
      UP_STAR = fromNumberUpStar(DyadicRational.ZERO, 1, 1);
      TWO = fromInteger(2);
      MINUS_TWO = fromInteger(-2);
   }

   // //////////////////////////////////////////////////////////////////////
   // Member data.

   // Options are stored in arrays rather than Collections. This is done to
   // keep the CanonicalGame class as fast and lean as possible.
   private CanonicalGame[]      leftOptions, rightOptions;

   // If this game is a number-up-star, then nusKey contains the corresponding
   // hash key. Otherwise nusKey is null.
   private NusKey               nusKey;

   // This game's birthday. Note that this is calculated during object
   // construction, so it is always valid.
   private int                  birthday;

   // A unique integer identifier for this game.
   private int                  id;

   // Various data are cached here when computed.
   private CanonicalGame        inverse;
   private Thermograph          thermograph;

   // //////////////////////////////////////////////////////////////////////
   // Private constructor

   private CanonicalGame()
   {
      id = nextID++;
      idCatalog.add(this);
   }

   // //////////////////////////////////////////////////////////////////////
   // "Surrogate constructors"

   public static CanonicalGame fromID(int id)
   {
      if (id < 0 || id >= nextID)
      {
         System.out.println(nextID);
         throw new IllegalArgumentException("id = " + id);
      }
      return (CanonicalGame) idCatalog.get(id);
   }

   /**
    * Returns the <code>CanonicalGame</code> corresponding to
    * <code>number</code>.
    * 
    * @param number
    *           The integer value of the <code>CanonicalGame</code>.
    * @return The <code>CanonicalGame</code> corresponding to
    *         <code>number</code>.
    */
   public static CanonicalGame fromInteger(int number)
   {
      int lastDefined;
      if (number >= 0)
      {
         NusKey nk = new NusKey();
         for (lastDefined = number; lastDefined >= 0; lastDefined--)
         {
            nk.number = new DyadicRational(lastDefined, 1);
            if (nusCache.containsKey(nk))
            {
               break;
            }
         }
         CanonicalGame g = (CanonicalGame) nusCache.get(nk);
         for (int i = lastDefined + 1; i <= number; i++)
         {
            CanonicalGame h = new CanonicalGame();
            h.leftOptions = new CanonicalGame[1];
            h.leftOptions[0] = g;
            h.rightOptions = new CanonicalGame[0];
            h.nusKey = new NusKey(new DyadicRational(i, 1), 0, 0);
            h.birthday = i;
            nusCache.put(h.nusKey, h);
            gameCache.put(new OptionsKey(h.leftOptions, h.rightOptions), h);
            g = h;
         }
         return g;
      } else
      {
         NusKey nk = new NusKey();
         for (lastDefined = number; lastDefined <= 0; lastDefined++)
         {
            nk.number = new DyadicRational(lastDefined, 1);
            if (nusCache.containsKey(nk))
            {
               break;
            }
         }
         CanonicalGame g = (CanonicalGame) nusCache.get(nk);
         for (int i = lastDefined - 1; i >= number; i--)
         {
            CanonicalGame h = new CanonicalGame();
            h.leftOptions = new CanonicalGame[0];
            h.rightOptions = new CanonicalGame[1];
            h.rightOptions[0] = g;
            h.nusKey = new NusKey(new DyadicRational(i, 1), 0, 0);
            h.birthday = -i;
            nusCache.put(h.nusKey, h);
            gameCache.put(new OptionsKey(h.leftOptions, h.rightOptions), h);
            g = h;
         }
         return g;
      }
   }

   /**
    * Returns a <code>CanonicalGame</code> equal to the sum of a number, a
    * nimber, and arbitrarily many copies of up. Specifically, the return value
    * is equal to the sum of:
    * <ul>
    * <li>The dyadic rational <code>number</code>,
    * <li><code>upMultiple</code> copies of up, and
    * <li>The nimber of order <code>nimber</code>.
    * </ul>
    * For example, <code>fromNumberUpStar(DyadicRational.ZERO, -2, 1)</code>
    * would return double-down-star.
    * 
    * @param number
    *           A {@link DyadicRational} that specifies the number component of
    *           this <code>CanonicalGame</code>.
    * @param upMultiple
    *           An integer specifying the number of copies of up in this
    *           <code>CanonicalGame</code>.
    * @param nimber
    *           An non-negative integer specifying the order of the nimber
    *           component of this <code>CanonicalGame</code>.
    * @return The sum of <code>number</code>, <code>nimber</code>, and
    *         <code>upMultiple</code> copies of up.
    * @throws IllegalArgumentException
    *            <code>number</code> is not finite.
    * @throws IllegalArgumentException
    *            <code>nimber</code> is negative.
    */
   public static CanonicalGame fromNumberUpStar(DyadicRational number, int upMultiple, int nimber)
   {
      // We can check the cache before validating the inputs, to save time.
      // If a cache entry is found then the inputs must be valid anyway.
      NusKey nusKey = new NusKey(number, upMultiple, nimber);
      CanonicalGame g = (CanonicalGame) nusCache.get(nusKey);
      if (g != null)
      {
         return g;
      }

      if (number == null)
      {
         throw new NullPointerException("number cannot be null.");
      }
      if (number.isInfinite())
      {
         throw new IllegalArgumentException("number must be finite.");
      }
      if (nimber < 0)
      {
         throw new IllegalArgumentException("nimber must be non-negative.");
      }

      g = new CanonicalGame();
      g.nusKey = nusKey;
      if (upMultiple == 0 && nimber == 0)
      {
         // Just a number.
         if (number.getDenominator() == 1)
         {
            return fromInteger(number.getNumerator());
         } else
         // A number but not an integer.
         {
            g = new CanonicalGame();
            g.nusKey = nusKey;
            g.leftOptions = new CanonicalGame[1];
            g.rightOptions = new CanonicalGame[1];
            // To find the canonical left option, we subtract 1/denominator.
            g.leftOptions[0] = fromNumberUpStar(new DyadicRational(number.getNumerator() - 1, number.getDenominator()), 0, 0);
            // To find the canonical right option, we add 1/denominator.
            g.rightOptions[0] = fromNumberUpStar(new DyadicRational(number.getNumerator() + 1, number.getDenominator()), 0, 0);
            g.birthday = Math.max(g.leftOptions[0].birthday, g.rightOptions[0].birthday) + 1;
         }
      } else if (upMultiple == 0)
      {
         // A number plus a nimber. First get the next lower nimber.
         CanonicalGame h = fromNumberUpStar(number, 0, nimber - 1);
         g.leftOptions = new CanonicalGame[nimber];
         g.rightOptions = g.leftOptions;
         for (int i = 0; i < nimber - 1; i++)
         {
            // Copy all of the next lower nimber's options.
            g.leftOptions[i] = h.leftOptions[i];
         }
         g.leftOptions[nimber - 1] = h;
         g.birthday = h.birthday + 1;
      } else if (upMultiple == 1 && nimber == 1)
      {
         // ^* needs to be handled as a special case.
         g.leftOptions = new CanonicalGame[2];
         g.rightOptions = new CanonicalGame[1];
         g.leftOptions[0] = g.rightOptions[0] = fromNumberUpStar(number, 0, 0);
         g.leftOptions[1] = fromNumberUpStar(number, 0, 1);
         g.birthday = g.leftOptions[1].birthday + 1;
      } else if (upMultiple == -1 && nimber == 1)
      {
         // Likewise with v*.
         g.leftOptions = new CanonicalGame[1];
         g.rightOptions = new CanonicalGame[2];
         g.leftOptions[0] = g.rightOptions[0] = fromNumberUpStar(number, 0, 0);
         g.rightOptions[1] = fromNumberUpStar(number, 0, 1);
         g.birthday = g.rightOptions[1].birthday + 1;
      } else if (upMultiple > 0)
      {
         g.leftOptions = new CanonicalGame[1];
         g.rightOptions = new CanonicalGame[1];
         g.leftOptions[0] = fromNumberUpStar(number, 0, 0);
         g.rightOptions[0] = fromNumberUpStar(number, upMultiple - 1, nimber ^ 1);
         g.birthday = g.rightOptions[0].birthday + 1;
      } else
      {
         g.leftOptions = new CanonicalGame[1];
         g.rightOptions = new CanonicalGame[1];
         g.leftOptions[0] = fromNumberUpStar(number, upMultiple + 1, nimber ^ 1);
         g.rightOptions[0] = fromNumberUpStar(number, 0, 0);
         g.birthday = g.leftOptions[0].birthday + 1;
      }
      nusCache.put(g.nusKey, g);
      gameCache.put(new OptionsKey(g.leftOptions, g.rightOptions), g);
      return g;
   }

   /**
    * Returns the <code>CanonicalGame</code> corresponding to tiny-
    * <code>g</code>. The return value is equal to the canonical form of
    * <code>{0||0|-g}</code>.
    * 
    * @param g
    *           The subscript of the tiny.
    * @return The <code>CanonicalGame</code> corresponding to tiny-
    *         <code>g</code>.
    * @see #fromMiny(CanonicalGame) fromMiny
    */
   public static CanonicalGame fromTiny(CanonicalGame g)
   {
      return fromOptions(ZERO, fromOptions(ZERO, (CanonicalGame) g.getInverse()));
   }

   /**
    * Returns the <code>CanonicalGame</code> corresponding to miny-
    * <code>g</code>. The return value is equal to the canonical form of
    * <code>{g|0||0}</code>.
    * 
    * @param g
    *           The subscript of the miny.
    * @return The <code>CanonicalGame</code> corresponding to tiny-
    *         <code>g</code>.
    * @see #fromTiny(CanonicalGame) fromTiny
    */
   public static CanonicalGame fromMiny(CanonicalGame g)
   {
      return fromOptions(fromOptions(g, ZERO), ZERO);
   }

   /**
    * Returns the canonical form of <code>g<sup>n</sup></code>. <code>g</code>
    * must be of the form <code>{0|h}</code>.
    * <p>
    * <code>g<sup>n</sup></code> is defined as follows:
    * <p>
    * <code>g<sup>0</sup> = -h</code><br>
    * <code>g<sup>n</sup> = {0|h-g<sup>&rarr;n-1</sup>} =
    * {0|-g<sup>0</sup>-g<sup>1</sup>-...-g<sup>n-1</sup>}</code>
    * 
    * @param g
    *           The game that serves as a base.
    * @param n
    *           The exponent.
    * @return A <code>CanonicalGame</code> corresponding to
    *         <code>g<sup>n</sup></code>.
    * @throws IllegalArgumentException
    *            <code>g</code> is not of the form <code>{0|h}</code>.
    * @see #fromGToNth(CanonicalGame, int) fromGToNth
    */
   public static CanonicalGame fromGNth(CanonicalGame g, int n)
   {
      if (g.leftOptions.length != 1 || !g.leftOptions[0].equals(ZERO) || g.rightOptions.length != 1)
      {
         throw new IllegalArgumentException("g must be of the form {0|h}.");
      }
      if (n == 0)
      {
         return (CanonicalGame) g.rightOptions[0].getInverse();
      } else
      {
         return fromOptions(ZERO, g.rightOptions[0].minus(fromGToNth(g, n - 1)));
      }
   }

   /**
    * Returns the canonical form of <code>g<sup>&rarr;n</sup></code>.
    * <code>g</code> must be of the form <code>{0|h}</code>.
    * <p>
    * <code>g<sup>&rarr;n</sup></code> is defined as follows:
    * <p>
    * <code>g<sup>&rarr;0</sup> = 0</code><br>
    * <code>g<sup>&rarr;n</sup> = {g<sup>&rarr;n-1</sup>|h} =
    * g<sup>1</sup>+g<sup>2</sup>+...+g<sup>n-1</sup></code>
    * 
    * @param g
    *           The game that serves as a base.
    * @param n
    *           The exponent.
    * @return A <code>CanonicalGame</code> corresponding to
    *         <code>g<sup>&rarr;n</sup></code>.
    * @throws IllegalArgumentException
    *            <code>g</code> is not of the form <code>{0|h}</code>.
    * @see #fromGNth(CanonicalGame, int) fromGNth
    */
   public static CanonicalGame fromGToNth(CanonicalGame g, int n)
   {
      if (g.leftOptions.length != 1 || !g.leftOptions[0].equals(ZERO) || g.rightOptions.length != 1)
      {
         throw new IllegalArgumentException("g must be of the form {0|h}.");
      }
      if (n == 0)
      {
         return ZERO;
      } else
      {
         return fromOptions(fromGToNth(g, n - 1), g.rightOptions[0]);
      }
   }

   /**
    * Returns the superstar with the specified exponents. This is defined as
    * <p>
    * <code>&uarr;<sup>a,b,c,...</sup> = {0,*,...,*m | *a,*b,*c,...}</code>
    * where <code>m = mex{a,b,c,...}</code>.
    * 
    * @param exponents
    *           The integers for the superstar exponents.
    * @return The corresponding superstar.
    * @throws IllegalArgumentException
    *            <code>exponents</code> is empty or contains a negative integer.
    */
   public static CanonicalGame fromSuperstar(int[] exponents)
   {
      if (exponents.length == 0)
      {
         throw new IllegalArgumentException("Exponent set cannot be empty.");
      }
      CanonicalGame[] rightOptions = new CanonicalGame[exponents.length];
      for (int i = 0; i < exponents.length; i++)
      {
         rightOptions[i] = fromNumberUpStar(DyadicRational.ZERO, 0, exponents[i]);
      }
      Arrays.sort(rightOptions);
      int mex = 0;
      for (int j = 0; j < rightOptions.length; j++)
      {
         if (j < rightOptions.length - 1 && rightOptions[j].equals(rightOptions[j + 1]))
         {
            rightOptions[j] = null;
         } else if (rightOptions[j].nusKey.nimber == mex)
         {
            // Ok to do it this way since options are sorted
            mex++;
         }
      }
      CanonicalGame[] leftOptions = new CanonicalGame[mex + 1];
      for (int i = 0; i <= mex; i++)
      {
         leftOptions[i] = fromNumberUpStar(DyadicRational.ZERO, 0, i);
      }
      return fromCanonicalOptions(leftOptions, pack(rightOptions));
   }

   /**
    * Returns a <code>CanonicalGame</code> based on collections of options. The
    * specified options need not be in canonical form; however,
    * <code>fromOptions</code> will canonicalize them by calling each option's
    * <code>canonicalize</code> method.
    * 
    * @param leftOptions
    *           A <code>Collection</code> whose elements are instances of
    *           <code>Game</code>.
    * @param rightOptions
    *           A <code>Collection</code> whose elements are instances of
    *           <code>Game</code>.
    * @return The canonical form of <code>{G<sup>L</sup>|G<sup>R</sup>}</code>,
    *         where <code>G<sup>L</sup></code> is the set of games in
    *         <code>leftOptions</code> and <code>G<sup>R</sup></code> is the set
    *         of games in <code>rightOptions</code>.
    * @throws IllegalArgumentException
    *            Some element of <code>leftOptions</code> or
    *            <code>rightOptions</code> is not a short game.
    */
   public static CanonicalGame fromOptions(Collection leftOptions, Collection rightOptions)
   {
      LinkedList leftOptionList = new LinkedList(), rightOptionList = new LinkedList();
      try
      {
         for (Iterator i = leftOptions.iterator(); i.hasNext();)
         {
            leftOptionList.add(((Game) i.next()).canonicalize());
         }
         for (Iterator i = rightOptions.iterator(); i.hasNext();)
         {
            rightOptionList.add(((Game) i.next()).canonicalize());
         }
      } catch (ClassCastException exc)
      {
         throw new IllegalArgumentException();
      } catch (NotShortGameException exc)
      {
         throw new IllegalArgumentException();
      }
      CanonicalGame[] leftOptionArray = new CanonicalGame[leftOptionList.size()], rightOptionArray = new CanonicalGame[rightOptionList.size()];
      leftOptionList.toArray(leftOptionArray);
      rightOptionList.toArray(rightOptionArray);
      return fromOptions(leftOptionArray, rightOptionArray);
   }

   static CanonicalGame fromOptions(CanonicalGame[] leftOptionArray, CanonicalGame[] rightOptionArray)
   {
      // Do a first pass to eliminate duplicate options (it's fast!)
      eliminateDuplicateOptions(leftOptionArray);
      eliminateDuplicateOptions(rightOptionArray);

      // Iteratively bypass all reversible moves (this will not add any
      // extra duplicate options.)
      leftOptionArray = bypassReversibleOptionsL(leftOptionArray, rightOptionArray);
      rightOptionArray = bypassReversibleOptionsR(leftOptionArray, rightOptionArray);

      // Now eliminate dominated options.
      eliminateDominatedOptions(leftOptionArray, true);
      eliminateDominatedOptions(rightOptionArray, false);

      return fromCanonicalOptions(pack(leftOptionArray), pack(rightOptionArray));
   }

   // Unlike fromOptions(CanonicalGame[], CanonicalGame[]), this method ASSUMES
   // that the supplied arrays contain no dominated or reversible options, and
   // no null entries. Passing unsimplified arrays to this method will
   // "seriously screw up everything" :)
   private static CanonicalGame fromCanonicalOptions(CanonicalGame[] leftOptionArray, CanonicalGame[] rightOptionArray)
   {
      Arrays.sort(leftOptionArray);
      Arrays.sort(rightOptionArray);
      OptionsKey optionsKey = new OptionsKey(leftOptionArray, rightOptionArray);
      CanonicalGame g = (CanonicalGame) gameCache.get(optionsKey);
      if (g != null)
      {
         return g;
      }

      // It's a new game!
      g = new CanonicalGame();
      g.leftOptions = leftOptionArray;
      g.rightOptions = rightOptionArray;
      g.birthday = Math.max(g.leftOptions.length == 0 ? -1 : g.leftOptions[g.leftOptions.length - 1].birthday, g.rightOptions.length == 0 ? -1 : g.rightOptions[g.rightOptions.length - 1].birthday) + 1;
      g.detectShortcuts();
      gameCache.put(optionsKey, g);
      return g;
   }

   /**
    * Returns a <code>CanonicalGame</code> based on a single left option and a
    * single right option. This is a convenience method equivalent to
    * <code>fromOptions(Collections.singleton(leftOption),
    * Collections.singleton(rightOption))</code>.
    * 
    * @param leftOption
    *           The left option of this game.
    * @param rightOption
    *           The right option of this game.
    * @return The canonical form of <code>{G<sup>L</sup>|G<sup>R</sup>}</code>,
    *         where <code>G<sup>L</sup></code> is <code>leftOption</code> and
    *         <code>G<sup>R</sup></code> is <code>rightOption</code>.
    */
   public static CanonicalGame fromOptions(CanonicalGame leftOption, CanonicalGame rightOption)
   {
      return fromOptions(new CanonicalGame[] { leftOption }, new CanonicalGame[] { rightOption });
   }

   // //////////////////////////////////////////////////////////////////////
   // Interface implementations and overrides of methods inherited from
   // class Object

   /**
    * Compares this object to another <code>CanonicalGame</code> based on an an
    * arbitrary, instance-dependent total ordering.
    * 
    * @param obj
    *           The object to compare to.
    * @return -1 if <code>this</code> is less than <code>obj</code>, 0 if
    *         <code>this == obj</code>, 1 if <code>this</code> is greater than
    *         <code>obj</code>.
    * @throws NullPointerException
    *            <code>obj</code> is null.
    * @throws IllegalArgumentException
    *            <code>obj</code> is not an instance of
    *            <code>CanonicalGame</code>.
    */
   public int compareTo(Object obj)
   {
      // This is relatively crude - designed for speed. We compare first
      // by birthday, then by length of left option list, then length of
      // right option list, and finally by reference comparison.

      if (this == obj)
      {
         return 0;
      }

      if (obj == null)
      {
         throw new NullPointerException();
      }
      if (!(obj instanceof CanonicalGame))
      {
         throw new IllegalArgumentException();
      }

      CanonicalGame h = (CanonicalGame) obj;

      if (birthday < h.birthday)
      {
         return -1;
      }
      if (birthday > h.birthday)
      {
         return 1;
      }
      if (leftOptions.length < h.leftOptions.length)
      {
         return -1;
      }
      if (leftOptions.length > h.leftOptions.length)
      {
         return 1;
      }
      if (rightOptions.length < h.rightOptions.length)
      {
         return -1;
      }
      if (rightOptions.length > h.rightOptions.length)
      {
         return 1;
      }
      return this.hashCode() - h.hashCode();
   }

   public boolean equals(Object obj)
   {
      // We can just use reference equality!
      return this == obj;
   }

   private String toString(int[] height, boolean bracketAll)
   {
      String s;

      if (nusKey != null)
      {
         s = "";
         if (nusKey.upMultiple == 0 && nusKey.nimber == 0 || !nusKey.number.equals(DyadicRational.ZERO))
         {
            s += nusKey.number.toString();
         }
         switch (nusKey.upMultiple)
         {
            case 1:
               s += "^";
               break;
            case 2:
               s += "^^";
               break;
            case -1:
               s += "v";
               break;
            case -2:
               s += "vv";
               break;
            default:
               if (nusKey.upMultiple < 0)
               {
                  s += "v" + String.valueOf(-nusKey.upMultiple);
               } else if (nusKey.upMultiple > 0)
               {
                  s += "^" + String.valueOf(nusKey.upMultiple);
               }
               break;
         }
         switch (nusKey.nimber)
         {
            case 0:
               break;
            case 1:
               s += "*";
               break;
            default:
               s += "*" + String.valueOf(nusKey.nimber);
               break;
         }
      } else if (isNumberTiny())
      {
         // A tiny.
         s = "";
         if (!leftOptions[0].equals(ZERO))
         {
            s += leftOptions[0].toString();
         }
         s += "Tiny(" + rightOptions[0].rightOptions[0].minus(leftOptions[0]).getInverse().toString() + ")";
      } else if (((CanonicalGame) getInverse()).isNumberTiny())
      {
         // A miny.
         s = "";
         if (!rightOptions[0].equals(ZERO))
         {
            s += rightOptions[0].toString();
         }
         s += "Miny(" + leftOptions[0].leftOptions[0].minus(rightOptions[0]).toString() + ")";
      } else
      {
         int[] optionHeight = new int[1];
         int leftHeight = 0, rightHeight = 0;
         String leftOptionString, rightOptionString;
         if (leftOptions.length == 1)
         {
            leftOptionString = leftOptions[0].toString(optionHeight, bracketAll);
            leftHeight = optionHeight[0];
         } else
         {
            leftHeight = 0;
            leftOptionString = "";
            // If there's more than one left option, we need to bracket each
            // left option of height > 0, and maxLeftHeight is automatically 0.
            for (int i = 0; i < leftOptions.length; i++)
            {
               String t = leftOptions[i].toString(optionHeight, bracketAll);
               if (optionHeight[0] == 0)
               {
                  leftOptionString += t;
               } else
               {
                  leftOptionString += "{" + t + "}";
               }
               if (i < leftOptions.length - 1)
               {
                  leftOptionString += ",";
               }
            }
         }
         if (rightOptions.length == 1)
         {
            rightOptionString = rightOptions[0].toString(optionHeight, bracketAll);
            rightHeight = optionHeight[0];
         } else
         {
            rightHeight = 0;
            rightOptionString = "";
            for (int i = 0; i < rightOptions.length; i++)
            {
               String t = rightOptions[i].toString(optionHeight, bracketAll);
               if (optionHeight[0] == 0)
               {
                  rightOptionString += t;
               } else
               {
                  rightOptionString += "{" + t + "}";
               }
               if (i < rightOptions.length - 1)
               {
                  rightOptionString += ",";
               }
            }
         }

         int h = Math.max(leftHeight, rightHeight) + 1;
         if (h == 3 || bracketAll)
         {
            s = "{";
            height[0] = 0;
         } else
         {
            s = "";
            height[0] = h;
         }
         s += leftOptionString;
         for (int i = 0; i < h; i++)
         {
            s += "|";
         }
         s += rightOptionString;
         if (h == 3 || bracketAll)
         {
            s += "}";
         }
      }
      return s;
   }

   public String toString()
   {
      int[] optionHeight = new int[] { 0 };
      String s = toString(optionHeight, false);
      if (optionHeight[0] == 0)
      {
         return s;
      } else
      {
         return "{" + s + "}";
      }
   }

   // //////////////////////////////////////////////////////////////////////
   // Implementation of Game

   public Collection getLeftOptions()
   {
      return Collections.unmodifiableCollection(Arrays.asList(leftOptions));
   }

   public Collection getRightOptions()
   {
      return Collections.unmodifiableCollection(Arrays.asList(rightOptions));
   }

   public Game getInverse()
   {
      if (inverse != null)
      {
         return inverse;
      }

      if (nusKey != null)
      {
         inverse = fromNumberUpStar(nusKey.number.getInverse(), -nusKey.upMultiple, nusKey.nimber);
      } else
      {
         CanonicalGame[] newLeftOptions = new CanonicalGame[rightOptions.length], newRightOptions = new CanonicalGame[leftOptions.length];

         for (int i = 0; i < newLeftOptions.length; i++)
         {
            newLeftOptions[i] = (CanonicalGame) rightOptions[i].getInverse();
         }
         for (int i = 0; i < newRightOptions.length; i++)
         {
            newRightOptions[i] = (CanonicalGame) leftOptions[i].getInverse();
         }
         inverse = fromCanonicalOptions(newLeftOptions, newRightOptions);
      }

      inverse.inverse = this; // (Might as well)
      return inverse;
   }

   public boolean isShortGame()
   {
      return true;
   }

   /**
    * Returns <code>this</code>.
    * 
    * @return <code>this</code>.
    */
   public CanonicalGame canonicalize()
   {
      return this;
   }

   public Game simplify()
   {
      return this;
   }

   public Game simplifyExpression(int simplifyType, Game[] args)
   {
      if (args != null && !(args[0] instanceof CanonicalGame))
      {
         return null;
      }
      CanonicalGame hc = (args == null ? null : (CanonicalGame) args[0]);
      switch (simplifyType)
      {
         case Game.SIMPLIFY_SUM:
            return plus(hc);
         case Game.SIMPLIFY_PRODUCT_G:
            return nortonProduct(hc);
         case Game.SIMPLIFY_ORDINAL_SUM_H:
            try
            {
               return new OrdinalSumGame(hc, this).canonicalize();
            } catch (NotShortGameException exc)
            {
               throw new RuntimeException();
            }
         case Game.SIMPLIFY_COOL:
            return cool(hc.getNumberPart());
         case Game.SIMPLIFY_FREEZE:
            return freeze();
         case Game.SIMPLIFY_HEAT:
            return heat(hc);
         case Game.SIMPLIFY_OVERHEAT:
            if (args[1] instanceof CanonicalGame)
               return overheat(hc, (CanonicalGame) args[1]);
            else
               return null;
         default:
            return null;
      }
   }

   // //////////////////////////////////////////////////////////////////////
   // Canonical operations.

   public int getID()
   {
      return id;
   }

   /**
    * Gets the birthday of this game. The birthday of G is defined recursively
    * by
    * <p>
    * <code>Birthday(G) = Max(Birthday(G<sup>L</sup>), Birthday(G<sup>R</sup>)) + 1</code>.
    * 
    * @return The birthday of this game.
    */
   public int getBirthday()
   {
      return birthday;
   }

   /**
    * Cools this game by <code>temperature</code> and returns the resulting
    * <code>CanonicalGame</code>.
    * 
    * @param temperature
    *           The temperature to cool by.
    * @return This game cooled by <code>temperature</code>.
    * @throws IllegalArgumentException
    *            <code>temperature</code> is negative or infinite.
    */
   public CanonicalGame cool(DyadicRational temperature)
   {
      System.out.println("cool");
      if (temperature.compareTo(DyadicRational.ZERO) < 0 || temperature.equals(DyadicRational.POSITIVE_INFINITY))
      {
         throw new IllegalArgumentException("Can only cool by a non-negative number.");
      } else
      {
         return cool(fromNumberUpStar(temperature, 0, 0));
      }
   }

   private CanonicalGame cool(CanonicalGame t)
   {
      if (isNumber())
      {
         return this;
      }

      if (getTemperature().compareTo(t.getNumberPart()) < 0)
      {
         return fromNumberUpStar(getMean(), 0, 0);
      }

      CanonicalGame[] newLeftOptions = new CanonicalGame[leftOptions.length], newRightOptions = new CanonicalGame[rightOptions.length];

      for (int i = 0; i < leftOptions.length; i++)
      {
         newLeftOptions[i] = leftOptions[i].cool(t).minus(t);
      }
      for (int i = 0; i < rightOptions.length; i++)
      {
         newRightOptions[i] = rightOptions[i].cool(t).plus(t);
      }

      return fromOptions(newLeftOptions, newRightOptions);
   }

   /**
    * Cools this game by its temperature and returns the resulting
    * <code>CanonicalGame</code>. This is a convenience method and is equivalent
    * to <code>cool(getTemperature())</code>.
    * 
    * @return This game cooled by its temperature.
    */
   public CanonicalGame freeze()
   {
      return cool(fromNumberUpStar(getTemperature(), 0, 0));
   }

   /**
    * Heats this game by <code>t</code> and returns the resulting
    * <code>CanonicalGame</code>.
    * <p>
    * Heating by an arbitrary <code>CanonicalGame</code> is permitted. Note that
    * heating a game by a negative number corresponds to the "unheating"
    * operation.
    * 
    * @param t
    *           The game to heat by.
    * @return This game heated by <code>t</code>.
    * @see #overheat(CanonicalGame, CanonicalGame) overheat
    */
   public CanonicalGame heat(CanonicalGame t)
   {
      if (isNumber())
      {
         return this;
      }

      CanonicalGame[] newLeftOptions = new CanonicalGame[leftOptions.length], newRightOptions = new CanonicalGame[rightOptions.length];

      for (int i = 0; i < leftOptions.length; i++)
      {
         newLeftOptions[i] = leftOptions[i].heat(t).plus(t);
      }
      for (int i = 0; i < rightOptions.length; i++)
      {
         newRightOptions[i] = rightOptions[i].heat(t).minus(t);
      }

      return fromOptions(newLeftOptions, newRightOptions);
   }

   /**
    * Overheats this game from <code>s</code> to <code>t</code> and returns the
    * resulting <code>CanonicalGame</code>.
    * <p>
    * Overheating by arbitrary <code>CanonicalGame</code>s is permitted.
    * 
    * @param s
    *           The "lower limit of integration."
    * @param t
    *           The "upper limit of integration."
    * @return This game overheated from <code>s</code> to </code>t</code>.
    * @see #heat(CanonicalGame) heat
    */
   public CanonicalGame overheat(CanonicalGame s, CanonicalGame t)
   {
      if (isInteger())
      {
         return nortonProduct(s); // G copies of s
      }

      CanonicalGame[] newLeftOptions = new CanonicalGame[leftOptions.length], newRightOptions = new CanonicalGame[rightOptions.length];

      for (int i = 0; i < leftOptions.length; i++)
      {
         newLeftOptions[i] = leftOptions[i].overheat(s, t).plus(t);
      }
      for (int i = 0; i < rightOptions.length; i++)
      {
         newRightOptions[i] = rightOptions[i].overheat(s, t).minus(t);
      }

      return fromOptions(newLeftOptions, newRightOptions);
   }

   /**
    * Calculates the sum of this game and <code>h</code> and returns the
    * resulting <code>CanonicalGame</code>.
    * 
    * @param h
    *           The game to add to this game.
    * @return The sum of this game and <code>h</code>.
    */
   public CanonicalGame plus(CanonicalGame h)
   {
      if (nusKey != null && h.nusKey != null)
      {
         return fromNumberUpStar(nusKey.number.plus(h.nusKey.number), nusKey.upMultiple + h.nusKey.upMultiple, nusKey.nimber ^ h.nusKey.nimber);
      }

      Map cache = Context.getActiveContext().getPrimaryCache();

      OperationKey ok = new OperationKey(OperationKey.OPERATION_TYPE_SUM, this, h);
      CanonicalGame sum = (CanonicalGame) cache.get(ok);
      if (sum != null)
      {
         return sum;
      }

      // If this is G and the input is H, we want to return
      // { GL+H, G+HL | GR+H, G+HR }

      int hStartLeftOption = isNumber() ? 0 : leftOptions.length, hStartRightOption = isNumber() ? 0 : rightOptions.length;

      CanonicalGame[] newLeftOptions = new CanonicalGame[hStartLeftOption + (h.isNumber() ? 0 : h.leftOptions.length)], newRightOptions = new CanonicalGame[hStartRightOption + (h.isNumber() ? 0 : h.rightOptions.length)];

      if (!isNumber()) // By the number translation theorem
      {
         for (int i = 0; i < leftOptions.length; i++)
         {
            newLeftOptions[i] = leftOptions[i].plus(h);
         }
         for (int i = 0; i < rightOptions.length; i++)
         {
            newRightOptions[i] = rightOptions[i].plus(h);
         }
      }
      if (!h.isNumber()) // By the number translation theorem
      {
         for (int i = 0; i < h.leftOptions.length; i++)
         {
            newLeftOptions[i + hStartLeftOption] = plus(h.leftOptions[i]);
         }
         for (int i = 0; i < h.rightOptions.length; i++)
         {
            newRightOptions[i + hStartRightOption] = plus(h.rightOptions[i]);
         }
      }

      sum = fromOptions(newLeftOptions, newRightOptions);
      cache.put(ok, sum);
      return sum;
   }

   /**
    * Calculates the difference of this game and <code>h</code> and returns the
    * resulting <code>CanonicalGame</code>.
    * <p>
    * <code>minus(h)</code> is equivalent to
    * <code>plus((CanonicalGame) h.getInverse())</code>.
    * 
    * @param h
    *           The game to subtract from this game.
    * @return The difference of this game and <code>h</code>.
    */
   public CanonicalGame minus(CanonicalGame h)
   {
      return plus((CanonicalGame) h.getInverse());
   }

   /**
    * Calculates the left stop of this game.
    * 
    * @return The left stop of this game.
    */
   public Stop getLeftStop()
   {
      if (isNumber())
      {
         return new Stop(nusKey.number, Stop.STOP_TYPE_LEFT);
      }
      Stop leftStop = leftOptions[0].getRightStop();
      for (int i = 1; i < leftOptions.length; i++)
      {
         Stop nextStop = leftOptions[i].getRightStop();
         if (leftStop.getStoppingValue().compareTo(nextStop.getStoppingValue()) < 0)
         {
            leftStop = nextStop;
         } else if (leftStop.getStoppingValue().equals(nextStop.getStoppingValue()) && leftStop.getStopType() != nextStop.getStopType())
         {
            leftStop = new Stop(leftStop.getStoppingValue(), Stop.STOP_TYPE_EITHER);
         }
      }
      return leftStop;
   }

   /**
    * Calculates the right stop of this game.
    * 
    * @return The right stop of this game.
    */
   public Stop getRightStop()
   {
      if (isNumber())
      {
         return new Stop(nusKey.number, Stop.STOP_TYPE_RIGHT);
      }
      Stop rightStop = rightOptions[0].getLeftStop();
      for (int i = 1; i < rightOptions.length; i++)
      {
         Stop nextStop = rightOptions[i].getLeftStop();
         if (nextStop.getStoppingValue().compareTo(rightStop.getStoppingValue()) < 0)
         {
            rightStop = nextStop;
         } else if (rightStop.getStoppingValue().equals(nextStop.getStoppingValue()) && rightStop.getStopType() != nextStop.getStopType())
         {
            rightStop = new Stop(rightStop.getStoppingValue(), Stop.STOP_TYPE_EITHER);
         }
      }
      return rightStop;
   }

   private int getFarStar()
   {
      if (isNimber())
      {
         return nusKey.nimber + 1;
      }
      int farStar = 1;
      for (int i = 0; i < leftOptions.length; i++)
      {
         farStar = Math.max(farStar, leftOptions[i].getFarStar());
      }
      for (int i = 0; i < rightOptions.length; i++)
      {
         farStar = Math.max(farStar, rightOptions[i].getFarStar());
      }
      return farStar;
   }

   /**
    * Calculates the atomic weight of this game. If this game is all small, the
    * standard definition of atomic weight is used. Otherwise, we use the
    * following algorithm, suggested by David Wolfe:
    * <p>
    * Apply the standard definition of atomic weight, even if the game is not
    * all small. Then check that the difference between <code>g</code> and
    * <code>G.&uarr;</code> is sufficiently small. Specifically, check that
    * <p>
    * <code>g-e <= G.^ <= g+e</code>
    * <p>
    * where e is the difference between <code>&uarr;*</code> and a long kite.
    * The value <code>G</code> will be returned regardless, and a warning will
    * be generated if the check fails.
    * 
    * @return The atomic weight of this game.
    * @throws UnsupportedOperationException
    *            This game is not an infinitesimal.
    */
   public CanonicalGame getAtomicWeight()
   {
      if (isAllSmall())
      {
         return getNaiveAtomicWeight();
      } else if (!isInfinitesimal())
      {
         throw new UnsupportedOperationException();
      }

      // We use the following algorithm suggested by David Wolfe:
      // Calculate the "naive atomic weight" using the standard method.
      // Then check that g-e <= G.^ <= g+e
      // where e is the difference of ^* and a long kite.

      CanonicalGame g = getNaiveAtomicWeight();
      CanonicalGame difference = this.minus(g.nortonProduct(UP));

      int farStar = getFarStar(), nextPow2 = 2;
      while (nextPow2 < farStar)
      {
         nextPow2 <<= 1;
      }

      CanonicalGame redKite;
      try
      {
         redKite = new OrdinalSumGame(fromNumberUpStar(DyadicRational.ZERO, 0, nextPow2), fromInteger(-1)).canonicalize();
      } catch (NotShortGameException exc)
      {
         throw new RuntimeException();
      }
      CanonicalGame e = UP_STAR.plus(redKite);

      if (!(difference.leq(e) && ((CanonicalGame) e.getInverse()).leq(difference)))
      {
         Context.getActiveContext().generateWarning("Warning: Atomic weight algorithm failed on a non-all-small game.");
      }

      return g;
   }

   private CanonicalGame getNaiveAtomicWeight()
   {
      if (nusKey != null)
      {
         return fromInteger(nusKey.upMultiple);
      }

      Map cache = Context.getActiveContext().getPrimaryCache();
      OperationKey ok = new OperationKey(OperationKey.OPERATION_TYPE_ATOMIC_WEIGHT, this, null);
      if (cache.containsKey(ok))
      {
         return (CanonicalGame) cache.get(ok);
      }

      CanonicalGame[] newLeftOptions = new CanonicalGame[leftOptions.length];
      CanonicalGame[] newRightOptions = new CanonicalGame[rightOptions.length];
      for (int i = 0; i < newLeftOptions.length; i++)
      {
         newLeftOptions[i] = leftOptions[i].getNaiveAtomicWeight().plus(MINUS_TWO);
      }
      for (int i = 0; i < newRightOptions.length; i++)
      {
         newRightOptions[i] = rightOptions[i].getNaiveAtomicWeight().plus(TWO);
      }
      CanonicalGame g, g0 = fromOptions((CanonicalGame[]) newLeftOptions.clone(), (CanonicalGame[]) newRightOptions.clone());
      if (g0.isInteger())
      {
         CanonicalGame farStar = fromNumberUpStar(DyadicRational.ZERO, 0, getFarStar());
         boolean leqFS = leq(farStar), geqFS = farStar.leq(this);
         if (leqFS && !geqFS)
         {
            if (newLeftOptions.length == 0)
            {
               g = CanonicalGame.ZERO;
            } else
            {
               // g < farStar. Now the least integer n0 such that n0 |> A is
               // calculated as follows:
               // If R(A) = L(n) or E(n) for some integer n then n0 = n.
               // If R(A) = R(n) for some integer n then n0 = n+1.
               // If R(A) = ?(x) for some noninteger x then n0 = ceiling(x).
               DyadicRational maxLeastInteger = DyadicRational.NEGATIVE_INFINITY;
               for (int i = 0; i < newLeftOptions.length; i++)
               {
                  DyadicRational leastInteger;
                  Stop rightStop = newLeftOptions[i].getRightStop();
                  if (rightStop.getStoppingValue().isInteger() && rightStop.getStopType() == Stop.STOP_TYPE_RIGHT)
                  {
                     leastInteger = rightStop.getStoppingValue().plus(1);
                  } else
                  {
                     leastInteger = rightStop.getStoppingValue().getCeiling();
                  }
                  if (maxLeastInteger.compareTo(leastInteger) < 0)
                  {
                     maxLeastInteger = leastInteger;
                  }
               }
               g = fromNumberUpStar(maxLeastInteger, 0, 0);
            }
         } else if (geqFS && !leqFS)
         {
            if (newRightOptions.length == 0)
            {
               g = CanonicalGame.ZERO;
            } else
            {
               // g > farStar. Now the greatest integer n0 such that n0 <| A is
               // calculated:
               // If L(A) = R(n) or E(n) for some integer n then n0 = n.
               // If L(A) = L(n) for some integer n then n0 = n-1.
               // If L(A) = ?(x) for some noninteger x then n0 = floor(x).
               DyadicRational minGreatestInteger = DyadicRational.POSITIVE_INFINITY;
               for (int i = 0; i < newRightOptions.length; i++)
               {
                  DyadicRational greatestInteger;
                  Stop leftStop = newRightOptions[i].getLeftStop();
                  if (leftStop.getStoppingValue().isInteger() && leftStop.getStopType() == Stop.STOP_TYPE_LEFT)
                  {
                     greatestInteger = leftStop.getStoppingValue().plus(-1);
                  } else
                  {
                     greatestInteger = leftStop.getStoppingValue().getFloor();
                  }
                  if (greatestInteger.compareTo(minGreatestInteger) < 0)
                  {
                     minGreatestInteger = greatestInteger;
                  }
               }
               g = fromNumberUpStar(minGreatestInteger, 0, 0);
            }
         } else
         {
            g = g0;
         }
      } else
      // Not an integer
      {
         g = g0;
      }

      cache.put(ok, g);
      return g;
   }

   /**
    * Calculates the Norton thermal dissociation of this game.
    * <p>
    * Every game is equal to its mean value plus the sum of heated
    * infinitesimals, and this representation is unique.
    * 
    * @return A {@link SumGame} representing the Norton thermal dissociation of
    *         this game. The first component is equal to the mean value of this
    *         game and the remaining components are {@link HeatedGame}s with
    *         infinitesimal integrand.
    */
   public Game dissociate()
   {
      List components = new ArrayList();
      CanonicalGame mean = fromNumberUpStar(getMean(), 0, 0);
      CanonicalGame remainder = minus(mean);
      components.add(mean);
      while (!remainder.equals(ZERO))
      {
         CanonicalGame temp = fromNumberUpStar(remainder.getTemperature(), 0, 0);
         CanonicalGame inf = remainder.cool(temp);
         Game nextComponent = new HeatedGame(inf, temp);
         components.add(nextComponent);
         remainder = remainder.minus(inf.heat(temp));
      }
      return new UnsimplifiedGame(new SumGame(components));
   }

   private CanonicalGame getStarProjection()
   {
      if (isNumber())
      {
         return this;
      } else if (isNumberUpStar() && getUpMultiplePart() == 0 && getNimberPart() == 1)
      {
         return fromNumberUpStar(getNumberPart(), 0, 0);
      }

      CanonicalGame[] newLeftOptions = new CanonicalGame[leftOptions.length], newRightOptions = new CanonicalGame[rightOptions.length];

      for (int i = 0; i < leftOptions.length; i++)
      {
         newLeftOptions[i] = leftOptions[i].getStarProjection();
      }
      for (int i = 0; i < rightOptions.length; i++)
      {
         newRightOptions[i] = rightOptions[i].getStarProjection();
      }

      return fromOptions(newLeftOptions, newRightOptions);
   }

   /**
    * Calculates the reduced canonical form of this game. The reduced canonical
    * form of g is the simplest game infinitesimally close to g.
    * 
    * @return The reduced canonical form of this game.
    */
   public CanonicalGame getRcf()
   {
      if (nusKey != null)
      {
         return fromNumberUpStar(nusKey.number, 0, 0);
      }
      return heat(STAR).getStarProjection();
   }

   public boolean leq(CanonicalGame h)
   {
      if (this == h)
      {
         return true;
      } else if (nusKey != null && h.nusKey != null)
      {
         return nusKey.number.compareTo(h.nusKey.number) < 0 || nusKey.number.equals(h.nusKey.number) && (nusKey.upMultiple < h.nusKey.upMultiple - 1 || nusKey.upMultiple < h.nusKey.upMultiple && (nusKey.nimber ^ h.nusKey.nimber) != 1);
      }

      boolean leq = true;

      // Return false if H <= GL for some left option GL of G
      // or HR <= G for some right option HR of H.
      // Otherwise return true.
      if (!isNumber()) // Number avoidance theorem
      {
         for (int i = 0; i < leftOptions.length; i++)
         {
            if (h.leq(leftOptions[i]))
            {
               leq = false;
               break;
            }
         }
      }
      if (leq && !h.isNumber()) // Can skip this if leq is already false
      {
         for (int i = 0; i < h.rightOptions.length; i++)
         {
            if (h.rightOptions[i].leq(this))
            {
               leq = false;
               break;
            }
         }
      }

      return leq;
   }

   /**
    * Gets the left incentives of this game.
    * 
    * @return The left incentives of this game.
    */
   public CanonicalGame[] getLeftIncentives()
   {
      CanonicalGame[] incentives = new CanonicalGame[leftOptions.length];
      for (int i = 0; i < leftOptions.length; i++)
      {
         incentives[i] = leftOptions[i].minus(this);
      }
      eliminateDuplicateOptions(incentives);
      eliminateDominatedOptions(incentives, true);
      return pack(incentives);
   }

   /**
    * Gets the right incentives of this game.
    * 
    * @return The right incentives of this game.
    */
   public CanonicalGame[] getRightIncentives()
   {
      CanonicalGame[] incentives = new CanonicalGame[rightOptions.length];
      for (int i = 0; i < rightOptions.length; i++)
      {
         incentives[i] = this.minus(rightOptions[i]);
      }
      eliminateDuplicateOptions(incentives);
      eliminateDominatedOptions(incentives, true);
      return pack(incentives);
   }

   /**
    * Gets the incentives of this game.
    * 
    * @return The incentives of this game.
    */
   public CanonicalGame[] getIncentives()
   {
      CanonicalGame[] incentives = new CanonicalGame[leftOptions.length + rightOptions.length];
      for (int i = 0; i < leftOptions.length; i++)
      {
         incentives[i] = leftOptions[i].minus(this);
      }
      for (int i = 0; i < rightOptions.length; i++)
      {
         incentives[leftOptions.length + i] = this.minus(rightOptions[i]);
      }
      eliminateDuplicateOptions(incentives);
      eliminateDominatedOptions(incentives, true);
      return pack(incentives);
   }

   /**
    * Calculates the Norton product of this game by the unit <code>u</code>.
    * <p>
    * If this game is an integer <code>n</code>, the result is equal to the sum
    * of <code>n</code> copies of <code>u</code>. A binary sum algorithm is used
    * for efficient calculation when <code>n</code> is large.
    * <p>
    * If this game is not an integer, the result is equal to...
    * 
    * @param u
    *           The unit of the Norton product.
    * @return The Norton product of this game by <code>u</code>.
    */
   public CanonicalGame nortonProduct(CanonicalGame u)
   {
      Map cache = Context.getActiveContext().getPrimaryCache();
      OperationKey ok = new OperationKey(OperationKey.OPERATION_TYPE_NORTON_PRODUCT, this, u);

      if (cache.containsKey(ok))
      {
         return (CanonicalGame) cache.get(ok);
      }

      CanonicalGame g = ZERO;

      if (isInteger())
      {
         int multiple = getNumberPart().getNumerator(), positiveMultiple = multiple < 0 ? -multiple : multiple;
         CanonicalGame binarySum = multiple < 0 ? (CanonicalGame) u.getInverse() : u;

         // We use a "binary addition" algorithm.
         for (int power2 = 0; positiveMultiple >> power2 != 0; power2++)
         {
            if (power2 > 0)
            {
               binarySum = binarySum.plus(binarySum);
            }
            if ((positiveMultiple & (1 << power2)) != 0)
            {
               g = g.plus(binarySum);
            }
         }
      } else
      {
         CanonicalGame[] uPlusIncentives = u.getIncentives();
         for (int i = 0; i < uPlusIncentives.length; i++)
         {
            uPlusIncentives[i] = u.plus(uPlusIncentives[i]);
         }
         CanonicalGame[] newLeftOptions = new CanonicalGame[leftOptions.length * uPlusIncentives.length], newRightOptions = new CanonicalGame[rightOptions.length * uPlusIncentives.length];

         for (int i = 0; i < leftOptions.length; i++)
         {
            CanonicalGame glDotU = leftOptions[i].nortonProduct(u);
            for (int j = 0; j < uPlusIncentives.length; j++)
            {
               newLeftOptions[i * uPlusIncentives.length + j] = glDotU.plus(uPlusIncentives[j]);
            }
         }
         for (int i = 0; i < rightOptions.length; i++)
         {
            CanonicalGame grDotU = rightOptions[i].nortonProduct(u);
            for (int j = 0; j < uPlusIncentives.length; j++)
            {
               newRightOptions[i * uPlusIncentives.length + j] = grDotU.minus(uPlusIncentives[j]);
            }
         }

         g = fromOptions(newLeftOptions, newRightOptions);
      }

      cache.put(ok, g);
      return g;
   }

   /**
    * Calculates the Conway product of this game and <code>h</code>. The Conway
    * product is defined and discussed in ONAG.
    * 
    * @param h
    *           The game by which to multiply this game.
    * @return The Conway product of this game and <code>h</code>.
    */
   public CanonicalGame conwayProduct(CanonicalGame h)
   {
      Map cache = Context.getActiveContext().getPrimaryCache();
      OperationKey ok = new OperationKey(OperationKey.OPERATION_TYPE_CONWAY_PRODUCT, this, h);

      if (cache.containsKey(ok))
      {
         return (CanonicalGame) cache.get(ok);
      }

      int ll = leftOptions.length, rl = rightOptions.length, hll = h.leftOptions.length, hrl = h.rightOptions.length;
      CanonicalGame newLeftOptions[] = new CanonicalGame[ll * hll + rl * hrl], newRightOptions[] = new CanonicalGame[ll * hrl + rl * hll];

      for (int i = 0; i < ll; i++)
      {
         for (int j = 0; j < hll; j++)
         {
            newLeftOptions[i * hll + j] = leftOptions[i].conwayProduct(h).plus(conwayProduct(h.leftOptions[j])).minus(leftOptions[i].conwayProduct(h.leftOptions[j]));
         }
      }
      for (int i = 0; i < rl; i++)
      {
         for (int j = 0; j < hrl; j++)
         {
            newLeftOptions[ll * hll + i * hrl + j] = rightOptions[i].conwayProduct(h).plus(conwayProduct(h.rightOptions[j])).minus(rightOptions[i].conwayProduct(h.rightOptions[j]));
         }
      }
      for (int i = 0; i < ll; i++)
      {
         for (int j = 0; j < hrl; j++)
         {
            newRightOptions[i * hrl + j] = leftOptions[i].conwayProduct(h).plus(conwayProduct(h.rightOptions[j])).minus(leftOptions[i].conwayProduct(h.rightOptions[j]));
         }
      }
      for (int i = 0; i < rl; i++)
      {
         for (int j = 0; j < hll; j++)
         {
            newRightOptions[ll * hrl + i * hll + j] = rightOptions[i].conwayProduct(h).plus(conwayProduct(h.leftOptions[j])).minus(rightOptions[i].conwayProduct(h.leftOptions[j]));
         }
      }
      CanonicalGame product = fromOptions(newLeftOptions, newRightOptions);
      cache.put(ok, product);
      return product;
   }

   /**
    * Returns <code>true</code> if this game is all small.
    * 
    * @return <code>true</code> if this game is all small, false otherwise.
    */
   public boolean isAllSmall()
   {
      if (nusKey != null)
      {
         return nusKey.number.equals(DyadicRational.ZERO);
      }
      for (int i = 0; i < leftOptions.length; i++)
      {
         if (!leftOptions[i].isAllSmall())
         {
            return false;
         }
      }
      for (int i = 0; i < rightOptions.length; i++)
      {
         if (!rightOptions[i].isAllSmall())
         {
            return false;
         }
      }
      return true;
   }

   public CanonicalGame[] orthodoxLeftOptions()
   {
      return findOrthodoxOptions(leftOptions, true);
   }

   public CanonicalGame[] orthodoxRightOptions()
   {
      return findOrthodoxOptions(rightOptions, false);
   }

   public CanonicalGame orthodoxForm()
   {
      CanonicalGame[] leftOrthodoxForms = new CanonicalGame[leftOptions.length];
      CanonicalGame[] rightOrthodoxForms = new CanonicalGame[rightOptions.length];
      for (int i = 0; i < leftOptions.length; i++)
      {
         leftOrthodoxForms[i] = leftOptions[i].orthodoxForm();
      }
      for (int i = 0; i < rightOptions.length; i++)
      {
         rightOrthodoxForms[i] = rightOptions[i].orthodoxForm();
      }
      return CanonicalGame.fromOptions(findOrthodoxOptions(leftOrthodoxForms, true), findOrthodoxOptions(rightOrthodoxForms, false));
   }

   private CanonicalGame[] findOrthodoxOptions(CanonicalGame[] options, boolean left)
   {
      Thermograph[] therms = new Thermograph[options.length];
      for (int i = 0; i < options.length; i++)
      {
         therms[i] = options[i].getThermograph();
      }
      LinkedList orthodoxOptions = new LinkedList();
      for (int i = options.length - 1; i >= 0; i--)
      {
         // Check to see if option i is orthodox. It's orthodox if there is
         // at least one "relevant temperature" for which it is strictly
         // better than all options still in the list. Relevant temperatures
         // include:
         // (i) the temperature of this game;
         // (ii) 0;
         // (iii) the option's even-indexed critical temperatures < the
         // temperature of this game.
         if (dominantAtTemperature(therms, left, i, getTemperature()) || dominantAtTemperature(therms, left, i, DyadicRational.ZERO))
         {
            orthodoxOptions.add(options[i]);
         } else
         {
            boolean added = false;
            // Look at the *right* trajectory of the *left* options, and
            // the *left* trajectory of the *right* options.
            DyadicRational[] criticalTemps = (left ? therms[i].rightTrajectory.criticalTemps : therms[i].leftTrajectory.criticalTemps);
            for (int k = 0; k < criticalTemps.length; k += 2)
            {
               if (criticalTemps[k].compareTo(getTemperature()) < 0 && criticalTemps[k].compareTo(DyadicRational.ZERO) > 0 && dominantAtTemperature(therms, left, i, criticalTemps[k]))
               {
                  orthodoxOptions.add(options[i]);
                  added = true;
                  break;
               }
            }
            if (!added)
            {
               therms[i] = null;
            }
         }
      }
      CanonicalGame[] orthodoxOptionsArray = new CanonicalGame[orthodoxOptions.size()];
      orthodoxOptions.toArray(orthodoxOptionsArray);
      return orthodoxOptionsArray;
   }

   // Checks if the thermograph at index i is dominant at temperature temp.
   // It's dominant provided it's strictly better than all other options still
   // in the list.
   private boolean dominantAtTemperature(Thermograph[] therms, boolean left, int i, DyadicRational temp)
   {
      for (int j = 0; j < therms.length; j++)
      {
         if (j != i && therms[j] != null && (left && (therms[i].rightValueAt(temp).compareTo(therms[j].rightValueAt(temp)) <= 0) || !left && (therms[i].leftValueAt(temp).compareTo(therms[j].leftValueAt(temp)) >= 0)))
         {
            return false;
         }
      }
      return true;
   }

   // //////////////////////////////////////////////////////////////////////
   // Other public methods

   /**
    * Gets the number of left options of this game.
    * 
    * @return The number of left options of this game.
    */
   public int getNumLeftOptions()
   {
      return leftOptions.length;
   }

   /**
    * Gets the <code>n</code><sup>th</sup> left option of this game.
    * 
    * @param n
    *           The index of the option to get.
    * @return The <code>n</code><sup>th</sup> left option of this game.
    * @throws IndexOutOfBoundsException
    *            <code>n < 0</code> or <code>n >= getNumLeftOptions()</code>.
    */
   public CanonicalGame getLeftOption(int n)
   {
      if (n < 0 || n >= leftOptions.length)
      {
         throw new IndexOutOfBoundsException("n");
      }
      return leftOptions[n];
   }

   /**
    * Gets the number of right options of this game.
    * 
    * @return The number of right options of this game.
    */
   public int getNumRightOptions()
   {
      return rightOptions.length;
   }

   /**
    * Gets the <code>n</code><sup>th</sup> right option of this game.
    * 
    * @param n
    *           The index of the option to get.
    * @return The <code>n</code><sup>th</sup> right option of this game.
    * @throws IndexOutOfBoundsException
    *            <code>n < 0</code> or <code>n >= getNumRightOptions()</code>.
    */
   public CanonicalGame getRightOption(int n)
   {
      if (n < 0 || n >= rightOptions.length)
      {
         throw new IndexOutOfBoundsException("n");
      }
      return rightOptions[n];
   }

   /**
    * Tests whether this game is equal to {@link #ZERO}.
    * 
    * @return <code>true</code> if this game is equal to <code>ZERO</code>.
    */
   public boolean isZero()
   {
      return this == ZERO || nusKey != null && nusKey.number.equals(DyadicRational.ZERO) && nusKey.upMultiple == 0 && nusKey.nimber == 0;
   }

   /**
    * Tests whether this game is equal to {@link #STAR}.
    * 
    * @return <code>true</code> if this game is equal to <code>STAR</code>.
    */
   public boolean isStar()
   {
      if (this == STAR || nusKey != null && nusKey.number.equals(DyadicRational.ZERO) && nusKey.upMultiple == 0 && nusKey.nimber == 1)
         return true;
      return false;
   }

   /**
    * Tests whether this game is a number.
    * 
    * @return <code>true</code> if this game is a number.
    */
   public boolean isNumber()
   {
      return nusKey != null && nusKey.upMultiple == 0 && nusKey.nimber == 0;
   }

   /**
    * Tests whether this game is an integer.
    * 
    * @return <code>true</code> if this game is an integer.
    */
   public boolean isInteger()
   {
      return isNumber() && nusKey.number.getDenominator() == 1;
   }

   /**
    * Tests whether this game is the sum of a number, a nimber, and a multiple
    * of up.
    * 
    * @return <code>true</code> if this game is the sum of a number, a nimber,
    *         and a multiple of up.
    */
   public boolean isNumberUpStar()
   {
      return nusKey != null;
   }

   /**
    * Tests whether this game is a nimber.
    * 
    * @return <code>true</code> if this game is a nimber.
    */
   public boolean isNimber()
   {
      return nusKey != null && nusKey.number.equals(DyadicRational.ZERO) && nusKey.upMultiple == 0;
   }

   /**
    * Tests whether this game is an infinitesimal.
    * 
    * @return <code>true</code> if this game is an infinitesimal.
    */
   public boolean isInfinitesimal()
   {
      if (nusKey == null)
      {
         return getLeftStop().getStoppingValue().equals(DyadicRational.ZERO) && getRightStop().getStoppingValue().equals(DyadicRational.ZERO);
      } else
      {
         return nusKey.number.equals(DyadicRational.ZERO);
      }
   }

   /**
    * Tests whether this game is equal to a number plus a tiny or a miny.
    * 
    * @return <code>true</code> if this game is a number plus a tiny or a miny.
    */
   public boolean isNumberTinyMiny()
   {
      return isNumberTiny() || isNumberMiny();
   }

   /**
    * Tests whether this game is equal to a number plus a miny.
    * 
    * @return <code>true</code> if this game is a number plus a miny.
    */
   public boolean isNumberMiny()
   {
      if (leftOptions.length != 1 || rightOptions.length != 1 || !rightOptions[0].isNumber() || leftOptions[0].leftOptions.length != 1 || leftOptions[0].rightOptions.length != 1 || rightOptions[0] != leftOptions[0].rightOptions[0])
      {
         return false;
      }
      return (leftOptions[0].leftOptions[0].getRightStop().getStoppingValue().compareTo(rightOptions[0].getNumberPart()) > 0);
   }

   /**
    * Tests whether this game is equal to a number plus a tiny.
    * 
    * @return <code>true</code> if this game is a number plus a tiny.
    */
   public boolean isNumberTiny()
   {
      if (leftOptions.length != 1 || rightOptions.length != 1 || !leftOptions[0].isNumber() || rightOptions[0].leftOptions.length != 1 || rightOptions[0].rightOptions.length != 1 || leftOptions[0] != rightOptions[0].leftOptions[0])
      {
         return false;
      }
      return (rightOptions[0].rightOptions[0].getLeftStop().getStoppingValue().compareTo(leftOptions[0].getNumberPart()) < 0);
   }

   /**
    * Tests whether this game is a switch. A <i>switch</i> is a game with the
    * same number of left and right options, in which every left option is the
    * inverse of a right option.
    * 
    * @return <code>true</code> if this game is a switch.
    */
   public boolean isSwitch()
   {
      if (leftOptions.length != rightOptions.length)
      {
         return false;
      }

      // We cannot simply test the left and right option arrays against each
      // other - there's no guarantee they are properly ordered.
      for (int i = 0; i < leftOptions.length; i++)
      {
         if (Arrays.binarySearch(rightOptions, leftOptions[i].getInverse()) < 0)
         {
            return false;
         }
      }
      return true;
   }

   /**
    * Gets the number part of a number-up-star. Note that
    * <code>isNumberUpStar()</code> must be <code>true</code> in order to call
    * this method.
    * 
    * @return The number part of this game.
    * @throws UnsupportedOperationException
    *            This game is not a number-up-star.
    */
   public DyadicRational getNumberPart()
   {
      if (nusKey == null)
      {
         throw new UnsupportedOperationException();
      } else
      {
         return nusKey.number;
      }
   }

   /**
    * Gets the up multiple part of a number-up-star. Note that
    * <code>isNumberUpStar()</code> must be <code>true</code> in order to call
    * this method.
    * 
    * @return The up multiple part of this game.
    * @throws UnsupportedOperationException
    *            This game is not a number-up-star.
    */
   public int getUpMultiplePart()
   {
      if (nusKey == null)
      {
         throw new UnsupportedOperationException();
      } else
      {
         return nusKey.upMultiple;
      }
   }

   /**
    * Gets the nimber part of a number-up-star. Note that
    * <code>isNumberUpStar()</code> must be <code>true</code> in order to call
    * this method.
    * 
    * @return The nimber part of this game.
    * @throws UnsupportedOperationException
    *            This game is not a number-up-star.
    */
   public int getNimberPart()
   {
      if (nusKey == null)
      {
         throw new UnsupportedOperationException();
      } else
      {
         return nusKey.nimber;
      }
   }

   public static String createReport()
   {
      return "Number of Distinct Canonical Games: " + gameCache.size() + "\n" + "Number of Games of Form m^n*k: " + nusCache.size() + "\n";
   }

   // //////////////////////////////////////////////////////////////////////
   // Simplification routines & shortcut detection

   private boolean leqArrays(CanonicalGame[] leftOptionArray, CanonicalGame[] rightOptionArray)
   {
      // Return false if H <= GL for some left option GL of G
      // or HR <= G for some right option HR of H.
      // Otherwise return true.

      for (int i = 0; i < leftOptions.length; i++)
      {
         if (leftOptions[i].geqArrays(leftOptionArray, rightOptionArray))
         {
            return false;
         }
      }
      for (int i = 0; i < rightOptionArray.length; i++)
      {
         if (rightOptionArray[i] != null && rightOptionArray[i].leq(this))
         {
            return false;
         }
      }
      return true;
   }

   private boolean geqArrays(CanonicalGame[] leftOptionArray, CanonicalGame[] rightOptionArray)
   {
      // Return false if GR <= H or G <= HL
      // Otherwise return true.

      for (int i = 0; i < rightOptions.length; i++)
      {
         if (rightOptions[i].leqArrays(leftOptionArray, rightOptionArray))
         {
            return false;
         }
      }
      for (int i = 0; i < leftOptionArray.length; i++)
      {
         if (leftOptionArray[i] != null && leq(leftOptionArray[i]))
         {
            return false;
         }
      }
      return true;
   }

   private static boolean arrayContains(CanonicalGame[] array, CanonicalGame g)
   {
      for (int i = 0; i < array.length; i++)
      {
         if (array[i] == g)
         {
            return true;
         }
      }
      return false;
   }

   private static void eliminateDuplicateOptions(CanonicalGame[] options)
   {
      Arrays.sort(options);
      for (int i = 0; i < options.length - 1; i++)
      {
         if (options[i] == options[i + 1])
         {
            options[i] = null;
         }
      }
   }

   private static void eliminateDominatedOptions(CanonicalGame[] options, boolean eliminateSmallerOptions)
   {
      for (int i = 0; i < options.length; i++)
      {
         if (options[i] != null)
            for (int j = 0; j < i; j++)
            {
               if (options[j] != null)
               {
                  if (eliminateSmallerOptions && options[i].leq(options[j]) || !eliminateSmallerOptions && options[j].leq(options[i]))
                  {
                     options[i] = null;
                     break;
                  } else if (eliminateSmallerOptions && options[j].leq(options[i]) || !eliminateSmallerOptions && options[i].leq(options[j]))
                  {
                     options[j] = null;
                  }
               }
            }
      }
   }

   private static CanonicalGame[] bypassReversibleOptionsL(CanonicalGame[] leftOptionArray, CanonicalGame[] rightOptionArray)
   {
      // Look for reversible moves for left.
      for (int i = 0; i < leftOptionArray.length; i++)
      {
         if (leftOptionArray[i] != null)
            for (int j = 0; j < leftOptionArray[i].rightOptions.length; j++)
            {
               if (leftOptionArray[i].rightOptions[j].leqArrays(leftOptionArray, rightOptionArray))
               {
                  CanonicalGame[] extraLeftOptions = leftOptionArray[i].rightOptions[j].leftOptions;
                  CanonicalGame[] newLeftOptionArray = new CanonicalGame[leftOptionArray.length - 1 + extraLeftOptions.length];
                  for (int k = 0; k < i; k++)
                  {
                     newLeftOptionArray[k] = leftOptionArray[k];
                  }
                  for (int k = i + 1; k < leftOptionArray.length; k++)
                  {
                     newLeftOptionArray[k - 1] = leftOptionArray[k];
                  }
                  for (int k = 0; k < extraLeftOptions.length; k++)
                  {
                     if (!arrayContains(leftOptionArray, extraLeftOptions[k]))
                     {
                        newLeftOptionArray[leftOptionArray.length - 1 + k] = extraLeftOptions[k];
                     }
                  }
                  leftOptionArray = newLeftOptionArray;
                  i--;
                  break;
               }
            }
      }
      return leftOptionArray;
   }

   private static CanonicalGame[] bypassReversibleOptionsR(CanonicalGame[] leftOptionArray, CanonicalGame[] rightOptionArray)
   {
      for (int i = 0; i < rightOptionArray.length; i++)
      {
         if (rightOptionArray[i] != null)
            for (int j = 0; j < rightOptionArray[i].leftOptions.length; j++)
            {
               if (rightOptionArray[i].leftOptions[j].geqArrays(leftOptionArray, rightOptionArray))
               {
                  CanonicalGame[] extraRightOptions = rightOptionArray[i].leftOptions[j].rightOptions;
                  CanonicalGame[] newRightOptionArray = new CanonicalGame[rightOptionArray.length - 1 + extraRightOptions.length];
                  for (int k = 0; k < i; k++)
                  {
                     newRightOptionArray[k] = rightOptionArray[k];
                  }
                  for (int k = i + 1; k < rightOptionArray.length; k++)
                  {
                     newRightOptionArray[k - 1] = rightOptionArray[k];
                  }
                  for (int k = 0; k < extraRightOptions.length; k++)
                  {
                     if (!arrayContains(rightOptionArray, extraRightOptions[k]))
                     {
                        newRightOptionArray[rightOptionArray.length - 1 + k] = extraRightOptions[k];
                     }
                  }
                  rightOptionArray = newRightOptionArray;
                  i--;
                  break;
               }
            }
      }
      return rightOptionArray;
   }

   private static CanonicalGame[] pack(CanonicalGame[] options)
   {
      int nOptions = 0;
      for (int i = 0; i < options.length; i++)
      {
         if (options[i] != null)
         {
            nOptions++;
         }
      }
      CanonicalGame[] packedOptions = new CanonicalGame[nOptions];
      for (int i = 0, j = 0; i < options.length; i++)
      {
         if (options[i] != null)
         {
            packedOptions[j] = options[i];
            j++;
         }
      }
      return packedOptions;
   }

   // Detects whether this is a number-up-star, etc.
   private void detectShortcuts()
   {
      if (leftOptions.length == 0)
      {
         // We just assume things are properly canonicalized and so right's
         // option list must be of length 1 with the unique element an
         // integer. Also this can never be 0 - since 0 is automatically
         // part of the game cache.
         nusKey = new NusKey(rightOptions[0].getNumberPart().plus(-1), 0, 0);
      } else if (rightOptions.length == 0)
      {
         nusKey = new NusKey(leftOptions[0].getNumberPart().plus(1), 0, 0);
      } else if (leftOptions.length == 1 && rightOptions.length == 1 && leftOptions[0].isNumber() && rightOptions[0].isNumber() && leftOptions[0].getNumberPart().compareTo(rightOptions[0].getNumberPart()) < 0)
      {
         // We're a number but not an integer. Conveniently, since the
         // option lists are canonicalized, the value of this game is the
         // mean of its left & right options.
         nusKey = new NusKey(DyadicRational.mean(leftOptions[0].getNumberPart(), rightOptions[0].getNumberPart()), 0, 0);
      } else if (leftOptions.length == 2 && rightOptions.length == 1 && leftOptions[0].isNumber() && leftOptions[0].equals(rightOptions[0]) && leftOptions[1].isNumberUpStar() && leftOptions[0].getNumberPart().equals(leftOptions[1].getNumberPart()) && leftOptions[1].getUpMultiplePart() == 0 && leftOptions[1].getNimberPart() == 1)
      {
         // For some number n, the form of this game is {n,n*|n} = n^*.
         nusKey = new NusKey(leftOptions[0].getNumberPart(), 1, 1);
      } else if (leftOptions.length == 1 && rightOptions.length == 2 && leftOptions[0].isNumber() && leftOptions[0].equals(rightOptions[0]) && rightOptions[1].isNumberUpStar() && rightOptions[0].getNumberPart().equals(rightOptions[1].getNumberPart()) && rightOptions[1].getUpMultiplePart() == 0 && rightOptions[1].getNimberPart() == 1)
      {
         // For some number n, the form of this game is {n|n,n*} = nv*.
         nusKey = new NusKey(leftOptions[0].getNumberPart(), -1, 1);
      } else if (leftOptions.length == 1 && rightOptions.length == 1 && leftOptions[0].isNumber() && rightOptions[0].isNumberUpStar() && !rightOptions[0].isNumber() && leftOptions[0].getNumberPart().equals(rightOptions[0].getNumberPart()) && rightOptions[0].getUpMultiplePart() >= 0)
      {
         // This is of the form n + {0|G} where G is a number-up-star of up
         // multiple >= 0.
         nusKey = new NusKey(rightOptions[0].getNumberPart(), rightOptions[0].getUpMultiplePart() + 1, rightOptions[0].getNimberPart() ^ 1);
      } else if (leftOptions.length == 1 && rightOptions.length == 1 && rightOptions[0].isNumber() && leftOptions[0].isNumberUpStar() && !leftOptions[0].isNumber() && leftOptions[0].getNumberPart().equals(rightOptions[0].getNumberPart()) && leftOptions[0].getUpMultiplePart() <= 0)
      {
         // This is of the form n + {G|0} where G is a number-up-star of up
         // multiple <= 0.
         nusKey = new NusKey(leftOptions[0].getNumberPart(), leftOptions[0].getUpMultiplePart() - 1, leftOptions[0].getNimberPart() ^ 1);
      } else if (leftOptions.length >= 1 && rightOptions.length >= 1 && leftOptions.length == rightOptions.length && leftOptions[0].isNumber() && leftOptions[0].equals(rightOptions[0]))
      {
         // Last we need to check for games of the form n + *k.
         DyadicRational mainNumber = leftOptions[0].getNumberPart();
         boolean isNimber = true;
         for (int i = 0; i < leftOptions.length; i++)
         {
            if (!leftOptions[i].equals(rightOptions[i]) || !leftOptions[i].isNumberUpStar() || !leftOptions[i].getNumberPart().equals(mainNumber) || leftOptions[i].getUpMultiplePart() != 0 || leftOptions[i].getNimberPart() != i)
            {
               isNimber = false;
               break;
            }
         }
         if (isNimber)
         {
            rightOptions = leftOptions; // Save some space.
            nusKey = new NusKey(mainNumber, 0, leftOptions.length);
         }
      }
      if (nusKey != null)
      {
         nusCache.put(nusKey, this);
      }
   }

   // Thermography: Mean and temperature calculations. Right now these work
   // only for canonical games. Hopefully they can be made more general in
   // the future.

   /**
    * Calculates the mean value of this game.
    * 
    * @return The mean value of this game.
    */
   public DyadicRational getMean()
   {
      if (nusKey != null)
      {
         return nusKey.number;
      } else
      {
         return getThermograph().getMast();
      }
   }

   /**
    * Calculates the temperature of this game.
    * 
    * @return The temperature of this game.
    */
   public DyadicRational getTemperature()
   {
      if (nusKey != null)
      {
         if (nusKey.nimber == 0 && nusKey.upMultiple == 0)
         {
            // It's a number k/2^n, so the temperature is -1/2^n
            return new DyadicRational(-1, nusKey.number.getDenominator());
         } else
         {
            // It's a number plus a nonzero infinitesimal
            return DyadicRational.ZERO;
         }
      } else
      {
         return getThermograph().getTemperature();
      }
   }

   /**
    * Gets the thermograph of this game.
    * 
    * @return The thermograph of this game.
    */
   public Thermograph getThermograph()
   {
      if (thermograph != null)
      {
         return thermograph;
      }

      if (isNumber())
      {
         Thermograph thermograph = new Thermograph();
         thermograph.leftTrajectory.mast = nusKey.number;
         thermograph.rightTrajectory.mast = nusKey.number;
         return thermograph;
      }

      Collection leftOptions = getLeftOptions(), rightOptions = getRightOptions();
      Thermograph.Trajectory[] leftOptionRTs = new Thermograph.Trajectory[leftOptions.size()], rightOptionLTs = new Thermograph.Trajectory[rightOptions.size()];
      Iterator iterator = getLeftOptions().iterator();
      int index = 0;
      while (iterator.hasNext())
      {
         leftOptionRTs[index] = ((CanonicalGame) iterator.next()).getThermograph().rightTrajectory;
         index++;
      }
      iterator = getRightOptions().iterator();
      index = 0;
      while (iterator.hasNext())
      {
         rightOptionLTs[index] = ((CanonicalGame) iterator.next()).getThermograph().leftTrajectory;
         index++;
      }

      // Build the left scaffold.
      int[] nextCriticalTemp = new int[leftOptionRTs.length];
      int currentDominantOption = -1;
      List newCriticalTemps = new ArrayList();
      Thermograph.Trajectory leftScaffold = new Thermograph.Trajectory();
      leftScaffold.mast = DyadicRational.NEGATIVE_INFINITY;
      for (int i = 0; i < leftOptionRTs.length; i++)
      {
         if (leftScaffold.mast.compareTo(leftOptionRTs[i].mast) < 0)
         {
            leftScaffold.mast = leftOptionRTs[i].mast;
            currentDominantOption = i;
         }
      }
      while (true)
      {
         // Advance each option so that nextCriticalTemp[i] points to the first
         // critical temp for which that option is greater than the dominant
         // option
         // (it's necessarily even-indexed.)
         int nextDominantOption = currentDominantOption;
         DyadicRational crossoverValue = DyadicRational.NEGATIVE_INFINITY;
         for (int i = 0; i < leftOptionRTs.length; i++)
         {
            if (i == currentDominantOption)
            {
               continue;
            }
            for (; nextCriticalTemp[i] <= leftOptionRTs[i].criticalTemps.length; nextCriticalTemp[i] += 2)
            {
               DyadicRational temp = leftOptionRTs[i].getCriticalTemp(nextCriticalTemp[i]);
               DyadicRational value = leftOptionRTs[i].mast.minus(leftOptionRTs[i].displacementAt(temp));
               if (leftOptionRTs[currentDominantOption].mast.minus(leftOptionRTs[currentDominantOption].displacementAt(temp)).compareTo(value) < 0)
               {
                  if (crossoverValue.compareTo(value) < 0)
                  {
                     nextDominantOption = i;
                     crossoverValue = value;
                  }
                  break;
               }
            }
         }
         // Now advance the current dominant option until it passes the
         // crossover value
         // (this necessarily happens at an odd index), adding critical temps at
         // each stage.
         for (; nextCriticalTemp[currentDominantOption] < leftOptionRTs[currentDominantOption].criticalTemps.length; nextCriticalTemp[currentDominantOption]++)
         {
            newCriticalTemps.add(leftOptionRTs[currentDominantOption].getCriticalTemp(nextCriticalTemp[currentDominantOption]));
            nextCriticalTemp[currentDominantOption]++;
            if (nextCriticalTemp[currentDominantOption] == leftOptionRTs[currentDominantOption].criticalTemps.length || leftOptionRTs[currentDominantOption].mast.minus(leftOptionRTs[currentDominantOption].displacementAt(leftOptionRTs[currentDominantOption].criticalTemps[nextCriticalTemp[currentDominantOption]])).compareTo(crossoverValue) < 0)
            {
               break;
            }
            newCriticalTemps.add(leftOptionRTs[currentDominantOption].getCriticalTemp(nextCriticalTemp[currentDominantOption]));
         }
         // Ok. Now the situation is (assuming a crossover value exists):
         // nextCriticalTemp[currentDominantOption] points to the first
         // critical temperature where it falls below crossoverValue
         // (necessarily an odd index) and nextCriticalTemp[nextDominantOption]
         // points to the first critical temperature where it reaches
         // crossoverValue (necessarily an even index).
         if (crossoverValue.equals(DyadicRational.NEGATIVE_INFINITY))
         {
            break; // We're done!!
         } else
         {
            DyadicRational criticalTemp = leftOptionRTs[currentDominantOption].getCriticalTemp(nextCriticalTemp[currentDominantOption]);
            newCriticalTemps.add(criticalTemp.plus(crossoverValue).minus(leftOptionRTs[currentDominantOption].mast).plus(leftOptionRTs[currentDominantOption].displacementAt(criticalTemp)));
            if (nextCriticalTemp[currentDominantOption] < leftOptionRTs[currentDominantOption].criticalTemps.length)
            {
               nextCriticalTemp[currentDominantOption]++;
            }
            currentDominantOption = nextDominantOption;
         }
      }
      leftScaffold.criticalTemps = new DyadicRational[newCriticalTemps.size()];
      newCriticalTemps.toArray(leftScaffold.criticalTemps);

      // Build the right scaffold.
      nextCriticalTemp = new int[rightOptionLTs.length];
      currentDominantOption = -1;
      newCriticalTemps = new ArrayList();
      Thermograph.Trajectory rightScaffold = new Thermograph.Trajectory();
      rightScaffold.mast = DyadicRational.POSITIVE_INFINITY;
      for (int i = 0; i < rightOptionLTs.length; i++)
      {
         if (rightOptionLTs[i].mast.compareTo(rightScaffold.mast) < 0)
         {
            rightScaffold.mast = rightOptionLTs[i].mast;
            currentDominantOption = i;
         }
      }
      while (true)
      {
         // Advance each option so that nextCriticalTemp[i] points to the first
         // critical temp for which that option is less than the dominant option
         // (it's necessarily even-indexed.)
         int nextDominantOption = currentDominantOption;
         DyadicRational crossoverValue = DyadicRational.POSITIVE_INFINITY;
         for (int i = 0; i < rightOptionLTs.length; i++)
         {
            if (i == currentDominantOption)
            {
               continue;
            }
            for (; nextCriticalTemp[i] <= rightOptionLTs[i].criticalTemps.length; nextCriticalTemp[i] += 2)
            {
               DyadicRational temp = rightOptionLTs[i].getCriticalTemp(nextCriticalTemp[i]);
               DyadicRational value = rightOptionLTs[i].mast.plus(rightOptionLTs[i].displacementAt(temp));
               if (value.compareTo(rightOptionLTs[currentDominantOption].mast.plus(rightOptionLTs[currentDominantOption].displacementAt(temp))) < 0)
               {
                  if (value.compareTo(crossoverValue) < 0)
                  {
                     nextDominantOption = i;
                     crossoverValue = value;
                  }
                  break;
               }
            }
         }
         // Now advance the current dominant option until it passes the
         // crossover value
         // (this necessarily happens at an odd index), adding critical temps at
         // each stage.
         for (; nextCriticalTemp[currentDominantOption] < rightOptionLTs[currentDominantOption].criticalTemps.length; nextCriticalTemp[currentDominantOption]++)
         {
            newCriticalTemps.add(rightOptionLTs[currentDominantOption].getCriticalTemp(nextCriticalTemp[currentDominantOption]));
            nextCriticalTemp[currentDominantOption]++;
            if (nextCriticalTemp[currentDominantOption] == rightOptionLTs[currentDominantOption].criticalTemps.length || crossoverValue.compareTo(rightOptionLTs[currentDominantOption].mast.plus(rightOptionLTs[currentDominantOption].displacementAt(rightOptionLTs[currentDominantOption].criticalTemps[nextCriticalTemp[currentDominantOption]]))) < 0)
            {
               break;
            }
            newCriticalTemps.add(rightOptionLTs[currentDominantOption].getCriticalTemp(nextCriticalTemp[currentDominantOption]));
         }
         // Ok. Now the situation is (assuming a crossover value exists):
         // nextCriticalTemp[currentDominantOption] points to the first
         // critical temperature where it falls below crossoverValue
         // (necessarily an odd index) and nextCriticalTemp[nextDominantOption]
         // points to the first critical temperature where it reaches
         // crossoverValue (necessarily an even index).
         if (crossoverValue.equals(DyadicRational.POSITIVE_INFINITY))
         {
            break; // We're done!!
         } else
         {
            DyadicRational criticalTemp = rightOptionLTs[currentDominantOption].getCriticalTemp(nextCriticalTemp[currentDominantOption]);
            newCriticalTemps.add(criticalTemp.minus(crossoverValue).plus(rightOptionLTs[currentDominantOption].mast).plus(rightOptionLTs[currentDominantOption].displacementAt(criticalTemp)));
            if (nextCriticalTemp[currentDominantOption] < rightOptionLTs[currentDominantOption].criticalTemps.length)
            {
               nextCriticalTemp[currentDominantOption]++;
            }
            currentDominantOption = nextDominantOption;
         }
      }
      rightScaffold.criticalTemps = new DyadicRational[newCriticalTemps.size()];
      newCriticalTemps.toArray(rightScaffold.criticalTemps);

      // Now build the new thermograph.
      int nextLeftCT = 0, nextRightCT = 0;
      DyadicRational crossTemp = null, crossValue = null;
      while (true)
      {
         DyadicRational nextTemp;
         if (leftScaffold.getCriticalTemp(nextLeftCT).compareTo(rightScaffold.getCriticalTemp(nextRightCT)) <= 0)
         {
            nextTemp = rightScaffold.getCriticalTemp(nextRightCT);
         } else
         {
            nextTemp = leftScaffold.getCriticalTemp(nextLeftCT);
         }
         DyadicRational nextLeftValue = leftScaffold.mast.minus(leftScaffold.displacementAt(nextTemp)).minus(nextTemp), nextRightValue = rightScaffold.mast.plus(rightScaffold.displacementAt(nextTemp)).plus(nextTemp);
         if (nextRightValue.compareTo(nextLeftValue) < 0)
         {
            if (nextLeftCT % 2 == 0 && nextRightCT % 2 == 0)
            {
               crossValue = nextLeftValue.plus(nextRightValue).over(2);
               crossTemp = nextTemp.plus(nextLeftValue.minus(nextRightValue).over(2));
            } else
            {
               crossTemp = nextTemp.plus(nextLeftValue.minus(nextRightValue));
               if (nextLeftCT % 2 == 0)
               {
                  crossValue = nextRightValue;
               } else
               {
                  crossValue = nextLeftValue;
               }
            }
            break;
         }
         if (nextLeftCT < leftScaffold.criticalTemps.length && leftScaffold.getCriticalTemp(nextLeftCT).equals(nextTemp))
         {
            nextLeftCT++;
         }
         if (nextRightCT < rightScaffold.criticalTemps.length && rightScaffold.getCriticalTemp(nextRightCT).equals(nextTemp))
         {
            nextRightCT++;
         }
      }
      thermograph = new Thermograph();
      thermograph.leftTrajectory.mast = thermograph.rightTrajectory.mast = crossValue;
      if (nextLeftCT % 2 == 0)
      {
         thermograph.leftTrajectory.criticalTemps = new DyadicRational[leftScaffold.criticalTemps.length - nextLeftCT + 1];
         thermograph.leftTrajectory.criticalTemps[0] = crossTemp;
         for (int i = nextLeftCT; i < leftScaffold.criticalTemps.length; i++)
         {
            thermograph.leftTrajectory.criticalTemps[i - nextLeftCT + 1] = leftScaffold.criticalTemps[i];
         }
      } else
      {
         thermograph.leftTrajectory.criticalTemps = new DyadicRational[leftScaffold.criticalTemps.length - nextLeftCT];
         for (int i = nextLeftCT; i < leftScaffold.criticalTemps.length; i++)
         {
            thermograph.leftTrajectory.criticalTemps[i - nextLeftCT] = leftScaffold.criticalTemps[i];
         }
      }
      if (nextRightCT % 2 == 0)
      {
         thermograph.rightTrajectory.criticalTemps = new DyadicRational[rightScaffold.criticalTemps.length - nextRightCT + 1];
         thermograph.rightTrajectory.criticalTemps[0] = crossTemp;
         for (int i = nextRightCT; i < rightScaffold.criticalTemps.length; i++)
         {
            thermograph.rightTrajectory.criticalTemps[i - nextRightCT + 1] = rightScaffold.criticalTemps[i];
         }
      } else
      {
         thermograph.rightTrajectory.criticalTemps = new DyadicRational[rightScaffold.criticalTemps.length - nextRightCT];
         for (int i = nextRightCT; i < rightScaffold.criticalTemps.length; i++)
         {
            thermograph.rightTrajectory.criticalTemps[i - nextRightCT] = rightScaffold.criticalTemps[i];
         }
      }
      return thermograph;
   }
}
