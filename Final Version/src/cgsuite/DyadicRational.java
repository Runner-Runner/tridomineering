/*
 * DyadicRational.java
 *
 * Created on October 17, 2002, 8:32 PM
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

import java.io.Serializable;

/**
 * A rational number whose denominator is a power of 2.
 * <p>
 * Support is also included for two special cases, {@link #POSITIVE_INFINITY} and
 * {@link #NEGATIVE_INFINITY}.  <code>POSITIVE_INFINITY</code> and
 * <code>NEGATIVE_INFINITY</code> behave as expected when compared to finite
 * rationals.
 * <p>
 * The results of <code>POSITIVE_INFINITY + NEGATIVE_INFINITY</code> and
 * <code>POSITIVE_INFINITY - POSITIVE_INFINITY</code> are not
 * defined, but for time efficiency no argument checking is performed.
 *
 * @author  Aaron Siegel
 * @version 0.1.1
 */
public class DyadicRational implements Comparable, Serializable
{
    /**
     * A static copy of 0.
     */
    public final static DyadicRational ZERO = new DyadicRational(0, 1);
    /**
     * A static copy of <code>POSITIVE_INFINITY</code>.
     */
    public final static DyadicRational POSITIVE_INFINITY = new DyadicRational(1, 0);
    /**
     * A static copy of <code>NEGATIVE_INFINITY</code>.
     */
    public final static DyadicRational NEGATIVE_INFINITY = new DyadicRational(-1, 0);
    
    private int numerator, denominator;
    
    /**
     * Constructs a new <code>DyadicRational</code> with the specified
     * numerator and denominator.
     * <p>
     * If <code>denominator</code> is <code>0</code>, then this equals
     * {@link #POSITIVE_INFINITY} if <code>numerator > 0</code> and
     * {@link #NEGATIVE_INFINITY} if <code>numerator < 0</code>.
     *
     * @param   numerator The numerator of this dyadic rational.
     * @param   denominator The denominator of this dyadic rational.
     * @throws  ArithmeticException <code>denominator</code> is neither
     *          <code>0</code> nor a power of 2.
     * @throws  ArithmeticException <code>numerator</code> and
     *          <code>denominator</code> are both equal to <code>0</code>.
     */
    public DyadicRational(int numerator, int denominator) throws ArithmeticException
    {
        if (denominator == 0 && numerator == 0)
        {
            throw new ArithmeticException("0/0");
        }
        
        while (numerator % 2 == 0 && denominator % 2 == 0)
        {
            numerator /= 2;
            denominator /= 2;
        }
        
        if (!validate(numerator, denominator))
        {
            throw new ArithmeticException("The denominator must be a power of 2.");
        }
        
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public boolean equals(DyadicRational a)
    {
        return numerator == a.numerator && denominator == a.denominator;
    }
    
    public int hashCode()
    {
        return (denominator << 16 + numerator);
    }

    /**
     * Compares this dyadic rational with the specified dyadic rational.
     * Following conventions set in <code>java.math.BigInteger</code>, this
     * method is provided in preference to individual methods for each of the
     * six boolean comparison operators.  The suggested usage is:
     * <p>
     * <code>x.compareTo(y)</code> &lt;<i>op</i>&rt; <code>0</code>
     * <p>
     * where &lt;<i>op</i>&rt; is a boolean comparator (such as
     * <code>&lt;=</code>).
     *
     * @param   a The dyadic rational to compare to this one.
     * @return  Negative if this is less than <code>a</code>, <code>0</code> if
     *          this is equal to <code>a</code>, or positive if this is greater
     *          than <code>a</code>.
     */
     
    public int compareTo(DyadicRational a)
    {
        if (denominator == 0)
        {
            // If both denominators are 0, just compare the numerators.
            // If this is infinite, but a is not, then just return this numerator.
            return (a.denominator == 0 ? numerator - a.numerator : numerator);
        }
        else if (a.denominator == 0)
        {
            // a is infinite, but this is not.
            return -a.numerator;
        }
        else if (denominator <= a.denominator)
        {
            return numerator * (a.denominator / denominator) - a.numerator;
        }
        else
        {
            return numerator - a.numerator * (denominator / a.denominator);
        }
    }
    
    /**
     * Compares this dyadic rational with the specified object.  If
     * <code>o</code> is a <code>DyadicRational</code>, this method behaves
     * like {@link #compareTo(DyadicRational)}; otherwise it throws a
     * <code>ClassCastException</code>.
     *
     * @param   o The object to compare to this dyadic rational.
     * @return  A negative, zero, or positive integer, depending on whether
     *          this dyadic rational is smaller than, equal to, or greater
     *          than <code>o</code>.
     * @see     #compareTo(DyadicRational)
     */
    public int compareTo(Object o)
    {
        return compareTo((DyadicRational) o);
    }

    /**
     * Gets the numerator of this dyadic rational.
     *
     * @return  The numerator of this dyadic rational.
     */
    public int getNumerator()
    {
        return numerator;
    }
    
    /**
     * Gets the denominator of this dyadic rational.
     *
     * @return  The denominator of this dyadic rational.
     */
    public int getDenominator()
    {
        return denominator;
    }
    
    /**
     * Returns <code>true</code> if the denominator is equal to 1.
     *
     * @return  <code>true</code> if the denominator is equal to 1.
     */
    public boolean isInteger()
    {
        return denominator == 1;
    }
    
    /**
     * Returns <code>true</code> if this dyadic rational is either
     * {@link #POSITIVE_INFINITY} or {@link #NEGATIVE_INFINITY}.
     *
     * @return  <code>true</code> if this dyadic rational is infinite.
     */
    public boolean isInfinite()
    {
        return denominator == 0;
    }
    
    /**
     * Gets the additive inverse of this dyadic rational.
     *
     * @return  The additive inverse of this dyadic rational.
     */
    public DyadicRational getInverse()
    {
        return new DyadicRational(-numerator, denominator);
    }
    
    /**
     * Calculates the sum of this dyadic rational and an integer.
     *
     * @param   n The integer to add to this dyadic rational.
     * @return  This dyadic rational plus <code>n</code>.
     */
    public DyadicRational plus(int n)
    {
        return new DyadicRational(numerator + n * denominator, denominator);
    }

    /**
     * Calculates the difference of this dyadic rational and an integer.
     *
     * @param   n The integer to subtract from this dyadic rational.
     * @return  This dyadic rational minus <code>n</code>.
     */
    public DyadicRational minus(int n)
    {
        return new DyadicRational(numerator - n * denominator, denominator);
    }
    
    /**
     * Calculates the product of this dyadic rational and an integer.
     *
     * @param   n The integer to multiply this dyadic rational by.
     * @return  This dyadic rational times <code>n</code>.
     */
    public DyadicRational times(int n)
    {
        return new DyadicRational(numerator * n, denominator);
    }
    
    /**
     * Calculates the quotient of this dyadic rational and an integer.
     *
     * @param   a The integer to divide this dyadic rational by.
     * @return  This dyadic rational divided by <code>n</code>.
     * @throws  ArithmeticException <code>n</code> is not a power of 2.
     */
    public DyadicRational over(int n)
    {
        if (n < 0)
        {
            return new DyadicRational(-numerator, -denominator * n);
        }
        else
        {
            return new DyadicRational(numerator, denominator * n);
        }
    }
    
    /**
     * Calculates the sum of this dyadic rational and <code>a</code>.
     *
     * @param   a The dyadic rational to add to this one.
     * @return  This dyadic rational plus <code>a</code>.
     */
    public DyadicRational plus(DyadicRational a)
    {
        if (denominator <= a.denominator)
        {
            return new DyadicRational(
                numerator * (a.denominator / denominator) + a.numerator,
                a.denominator
                );
        }
        else
        {
            return new DyadicRational(
                numerator + a.numerator * (denominator / a.denominator),
                denominator
                );
        }
    }
    
    /**
     * Calculates the difference of this dyadic rational and <code>a</code>.
     *
     * @param   a The dyadic rational to subtract from this one.
     * @return  This dyadic rational minus <code>a</code>.
     */
    public DyadicRational minus(DyadicRational a)
    {
        if (denominator <= a.denominator)
        {
            return new DyadicRational(
                numerator * (a.denominator / denominator) - a.numerator,
                a.denominator
                );
        }
        else
        {
            return new DyadicRational(
                numerator - a.numerator * (denominator / a.denominator),
                denominator
                );
        }
    }
    
    /**
     * Calculates the product of this dyadic rational and <code>a</code>.
     *
     * @param   a The dyadic rational to multiply this one by.
     * @return  This dyadic rational times <code>a</code>.
     */
    public DyadicRational times(DyadicRational a)
    {
        return new DyadicRational(numerator * a.numerator, denominator * a.denominator);
    }
    
    /**
     * Gets the floor of this dyadic rational.  Floor(a) is the largest integer
     * less than or equal to a.
     *
     * @return  The floor of this dyadic rational.
     */
    public DyadicRational getFloor()
    {
        if (denominator == 1 || denominator == 0)
        {
            return this;
        }
        if (numerator > 0)
        {
            return new DyadicRational(numerator / denominator, 1);
        }
        else
        {
            return new DyadicRational(numerator / denominator - 1, 1);
        }
    }
    
    /**
     * Gets the ceiling of this dyadic rational.  Ceiling(a) is the smallest
     * integer greater than or equal to a.
     *
     * @return  The ceiling of this dyadic rational.
     */
    public DyadicRational getCeiling()
    {
        if (denominator == 1 || denominator == 0)
        {
            return this;
        }
        if (numerator > 0)
        {
            return new DyadicRational(numerator / denominator + 1, 1);
        }
        else
        {
            return new DyadicRational(numerator / denominator, 1);
        }
    }
    
    /**
     * Converts this <code>DyadicRational</code> to a <code>double</code>.
     * This is equivalent to
     * <code>((double) getNumerator()) / ((double) getDenominator())</code>.
     *
     * @return  This dyadic rational converted to a <code>double</code>.
     */
    public double doubleValue()
    {
        if (denominator == 0)
        {
            return (numerator < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        }
        return ((double) numerator)/((double) denominator);
    }
    
    public String toString()
    {
        return numerator + (denominator == 1 ? "" : "/" + denominator);
    }
    
    /**
     * Returns the maximum of the two <code>DyadicRational</code>s.
     *
     * @return  The larger of <code>a</code> and <code>b</code>.
     */
    public static DyadicRational max(DyadicRational a, DyadicRational b)
    {
        return (a.compareTo(b) <= 0 ? b : a);
    }

    /**
     * Returns the arithmetic mean of the two <code>DyadicRational</code>s.
     *
     * @return  The mean of <code>a</code> and <code>b</code>.
     */
    public static DyadicRational mean(DyadicRational a, DyadicRational b)
    {
        return new DyadicRational(
            a.numerator * b.denominator + b.numerator * a.denominator,
            a.denominator * b.denominator * 2
            );
    }
    
    /**
     * Validates the specified numerator and denominator.  That is, checks
     * to make sure that <code>numerator</code> and <code>denominator</code>
     * constitute legitimate arguments to the {@link #DyadicRational(int, int)}
     * constructor.
     *
     * @param   numerator   The numerator to validate.
     * @param   denominator The denominator to validate.
     * @return  <code>true</code> if <code>numerator</code> and
     *          <code>denominator</code> are valid arguments.
     */
    public static boolean validate(int numerator, int denominator)
    {
        if (denominator == 0)
        {
            return numerator != 0;
        }
        
        // Now verify that the denominator is a power of two.
        while (denominator % 2 == 0)
        {
            denominator /= 2;
        }
        return denominator == 1;
    }
}
