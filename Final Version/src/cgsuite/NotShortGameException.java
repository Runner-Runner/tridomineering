/*
 * NotShortGameException.java
 *
 * Created on April 27, 2003, 3:24 PM
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
 * Thrown if a game's {@link Game#canonicalize() canonicalize} method is
 * called, and that game is not a short game.
 */
public class NotShortGameException extends Exception
{
    /**
     * Constructs a new instance of <code>NotShortGameException</code>.
     */
    public NotShortGameException()
    {
    }
    
    /**
     * Constructs a new instance of <code>NotShortGameException</code> with
     * the specified message.
     */
    public NotShortGameException(String message)
    {
        super(message);
    }
}
