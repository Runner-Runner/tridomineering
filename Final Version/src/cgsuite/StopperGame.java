/*
 * StopperGame.java
 *
 * Created on April 24, 2003, 6:45 PM
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
 * A convenience interface that exposes methods shared by
 * {@link CanonicalGame} and {@link CanonicalStopperGame}.
 */
public interface StopperGame extends Game
{
    /**
     * Gets the left stop of this game.  The left stop is the number that
     * is reached when this game is played in isolation, starting with left
     * to move, and assuming both players play optimally.
     *
     * @return  The left stop of this game.
     */
    Stop getLeftStop();
    
    /**
     * Gets the right stop of this game.  The right stop is the number that
     * is reached when this game is played in isolation, starting with right
     * to move, and assuming both players play optimally.
     *
     * @return  The right stop of this game.
     */
    Stop getRightStop();
}

