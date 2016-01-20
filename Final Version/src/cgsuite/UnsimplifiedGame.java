/*
 * UnsimplifiedGame.java
 *
 * Created on February 18, 2003, 6:49 PM
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
 * A wrapper for another <code>Game</code> that is immune to simplifications.
 * An instance of this class contains a reference to exactly one other game.
 * It behaves exactly as its component, except that any calls to
 * {@link #simplify() simplify} and
 * {@link #simplifyExpression(int, Game[]) simplifyExpression} will be
 * ignored.
 * <p>
 * This class is primarily useful when you want the Combinatorial Game Suite
 * interface to display a game in a form other than simplest form.  For
 * example, every canonical game has a unique representation as the sum of
 * heated infinitesimals.  In CGSuite, this "thermal dissociation" is wrapped
 * in an <code>UnsimplifiedGame</code>, since otherwise the sum would
 * be collapsed back into canonical form before it could be displayed to the
 * user.
 */
public class UnsimplifiedGame implements Game, Comparable
{
    private Game g;
    
    /**
     * Constructs a new <code>UnsimplifiedGame</code>.
     *
     * @param   g The game to wrap.
     */
    public UnsimplifiedGame(Game g)
    {
        this.g = g;
    }
    
    public boolean equals(Object o)
    {
        return o instanceof UnsimplifiedGame && ((UnsimplifiedGame) o).g.equals(g);
    }
    
    public int hashCode()
    {
        return g.hashCode();
    }
    
    public int compareTo(Object o)
    {
        if (o instanceof UnsimplifiedGame)
        {
            return Context.getActiveContext().getGameComparator().compare
                (g, ((UnsimplifiedGame) o).g);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
    
    public java.util.Collection getLeftOptions()
    {
        return g.getLeftOptions();
    }
    
    public java.util.Collection getRightOptions()
    {
        return g.getRightOptions();
    }
    
    public Game getInverse()
    {
        return new UnsimplifiedGame(g.getInverse());
    }
    
    public Game simplify()
    {
        return this;
    }
    
    public Game simplifyExpression(int simplifyType, Game[] args)
    {
        return null;
    }
    
    public boolean isShortGame()
    {
        return g.isShortGame();
    }
    
    public CanonicalGame canonicalize() throws NotShortGameException
    {
        return g.canonicalize();
    }
    
    /**
     * Gets the component of this game.
     *
     * @return  The component of this game.
     */
    public Game getG()
    {
        return g;
    }
    
    public String toString()
    {
        return g.toString();
    }
}
