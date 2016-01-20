/*
 * HeatedGame.java
 *
 * Created on November 4, 2002, 10:13 PM
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
 * A heated or overheated game.  The integrand and the limits of integration
 * can be arbitrary objects of type {@link Game}.
 *
 * @author  Aaron Siegel
 * @version 0.1.1
 */
public class HeatedGame implements Game, Comparable
{
    Game g, s, t;
    
    /**
     * Constructs a new <code>HeatedGame</code> that represents <code>g</code>
     * overheated from <code>s</code> to <code>t</code>.
     *
     * @param   g The integrand (that is, the game to be heated.)
     * @param   s The lower limit of integration.
     * @param   t The upper limit of integration.
     */
    public HeatedGame(Game g, Game s, Game t)
    {
        this.g = g;
        this.s = s;
        this.t = t;
    }
    
    /**
     * Constructs a new <code>HeatedGame</code> that represents <code>g</code>
     * heated by temperature <code>t</code>.
     *
     * @param   g The integrand (that is, the game to be heated.)
     * @param   t The upper limit of integration (corresponds to temperature
     *            when <code>t</code> is a {@link DyadicRational}.)
     */
    public HeatedGame(Game g, Game t)
    {
        this.g = g;
        this.s = null;
        this.t = t;
    }
    
    public boolean equals(Object o)
    {
        return o instanceof HeatedGame &&
            g.equals(((HeatedGame) o).g) &&
            s.equals(((HeatedGame) o).s) &&
            t.equals(((HeatedGame) o).t);
    }
    
    public int hashCode()
    {
        return ((g.hashCode() * 2047) + s.hashCode()) * 2047 + t.hashCode();
    }
    
    public int compareTo(Object o)
    {
        if (o instanceof HeatedGame)
        {
            HeatedGame other = (HeatedGame) o;
            if (s == null && other.s != null)
            {
                return -1;
            }
            else if (s != null && other.s == null)
            {
                return 1;
            }
            int cmp;
            java.util.Comparator comparator = Context.getActiveContext().getGameComparator();
            cmp = comparator.compare(g, other.g);
            if (cmp != 0) return cmp;
            if (s != null)
            {
                cmp = comparator.compare(s, other.s);
                if (cmp != 0) return cmp;
            }
            return comparator.compare(t, other.t);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
    
    public java.util.Collection getLeftOptions()
    {
        throw new UnsupportedOperationException();
    }
    
    public java.util.Collection getRightOptions()
    {
        throw new UnsupportedOperationException();
    }
    
    public Game getInverse()
    {
        return new HeatedGame(g.getInverse(), s, t);
    }
        
    public boolean isShortGame()
    {
        return g.isShortGame() && t.isShortGame() && (s == null || s.isShortGame());
    }
    
    public CanonicalGame canonicalize() throws NotShortGameException
    {
        if (s == null)
        {
            return g.canonicalize().heat(t.canonicalize());
        }
        else
        {
            return g.canonicalize().overheat(s.canonicalize(), t.canonicalize());
        }
    }
    
    public Game simplify()
    {
        Game gSimp = g.simplify(), tSimp = t.simplify(), sSimp = null;
        if (s != null)
        {
            sSimp = s.simplify();
        }
        Game simp;
        if (s == null)
        {
            simp = gSimp.simplifyExpression(Game.SIMPLIFY_HEAT, new Game[] {tSimp});
        }
        else
        {
            simp = gSimp.simplifyExpression
                (Game.SIMPLIFY_OVERHEAT, new Game[] {sSimp, tSimp});
        }
        if (simp == null)
        {
            return new HeatedGame(gSimp, sSimp, tSimp);
        }
        else
        {
            return simp;
        }
    }
    
    public Game simplifyExpression(int simplifyType, Game[] args)
    {
        return null;
    }
    
    /**
     * Gets the integrand (that is, the game being heated.)
     *
     * @return  The integrand.
     */
    public Game getG()
    {
        return g;
    }
    
    /**
     * Gets the lower limit of integration.
     *
     * @return  The lower limit of integration, or <code>null</code> if this
     *          game was constructed with {@link #HeatedGame(Game, Game)}.
     */
    public Game getS()
    {
        return s;
    }
    
    /**
     * Gets the upper limit of integration.
     *
     * @return  The upper limit of integration.
     */
    public Game getT()
    {
        return t;
    }
}
