/*
 * Stop.java
 *
 * Created on December 4, 2002, 9:18 PM
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

/**
 * The left or right stop of a game.  An instance of <code>Stop</code> contains
 * the following information:
 * <ul>
 * <li>The value of the stop.  For loopy games, this may be
 * {@link DyadicRational#POSITIVE_INFINITY POSITIVE_INFINITY} or
 * {@link DyadicRational#NEGATIVE_INFINITY NEGATIVE_INFINITY} to indicate that the
 * stopping position is {@link CanonicalStopperGame#ON ON} or
 * {@link CanonicalStopperGame#OFF OFF}, respectively;
 * <li>The player whose turn it is to move when the stopping position is
 * reached.
 * </ul>
 *
 * @author  Aaron Siegel
 */
public class Stop
{
    /**
     * Indicates that it is left's turn to move when the stopping position is
     * reached.
     */
    public final static int STOP_TYPE_LEFT = 0;
    /**
     * Indicates that it is right's turn to move when the stopping position is
     * reached.
     */
    public final static int STOP_TYPE_RIGHT = 1;
    /**
     * Indicates that the player in question can determine who has the move
     * when the stopping position is reached.
     */
    public final static int STOP_TYPE_EITHER = 2;
    
    private DyadicRational stoppingValue;
    private int stopType;

    /**
     * Constructs a new <code>Stop</code> with the given value and type.
     *
     * @param   initStoppingValue The value of this stop.
     * @param   initStopType One of <code>STOP_TYPE_LEFT</code>,
     *          <code>STOP_TYPE_RIGHT</code>, or <code>STOP_TYPE_EITHER</code>.
     * @throws  IllegalArgumentException <code>stopType</code> is not one of
     *          the values <code>STOP_TYPE_LEFT</code>,
     *          <code>STOP_TYPE_RIGHT</code>, or <code>STOP_TYPE_EITHER</code>.
     */
    public Stop(DyadicRational initStoppingValue, int initStopType)
    {
        if (stopType != STOP_TYPE_LEFT && stopType != STOP_TYPE_RIGHT &&
            stopType != STOP_TYPE_EITHER)
        {
            throw new IllegalArgumentException("stopType");
        }
        stoppingValue = initStoppingValue;
        stopType = initStopType;
    }

    public String toString()
    {
        String s;
        if (stopType == STOP_TYPE_LEFT)
        {
            s = "L";
        }
        else if (stopType == STOP_TYPE_RIGHT)
        {
            s = "R";
        }
        else
        {
            s = "E";
        }
        s += "(" + stoppingValue.toString() + ")";
        return s;
    }
    
    /**
     * Gets the value of this stop.  For loopy games, the value may be
     * {@link DyadicRational#POSITIVE_INFINITY POSITIVE_INFINITY} or
     * {@link DyadicRational#NEGATIVE_INFINITY NEGATIVE_INFINITY} to indicate that the
     * stopping position is {@link CanonicalStopperGame#ON ON} or
     * {@link CanonicalStopperGame#OFF OFF}, respectively.
     *
     * @return  The value of this stop.
     */
    public DyadicRational getStoppingValue()
    {
        return stoppingValue;
    }

    /**
     * Gets the stop type, indicating who has the move when the stopping
     * position is reached.  The value is one of the following:
     * <p>
     * <code>STOP_TYPE_LEFT</code> - It is left's turn to move when the
     * stopping position is reached.<br>
     * <code>STOP_TYPE_RIGHT</code> - It is right's turn to move when the
     * stopping position is reached.<br>
     * <code>STOP_TYPE_EITHER</code> - The player in question can determine
     * whose turn it is to move when the stopping position is reached.
     *
     * @return  The stop type.
     */
    public int getStopType()
    {
        return stopType;
    }
}
