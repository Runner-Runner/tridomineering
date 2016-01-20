/*
 * OrdinalSumGame.java
 *
 * Created on December 4, 2002, 12:45 AM
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

import java.util.*;

/**
 * The ordinal sum of two games.  The <i>ordinal sum</i> of <code>G</code> and
 * <code>H</code>, usually denoted <code>G:H</code>, is defined by
 * <p>
 * <code>G:H = {G<sup>L</sup>, G:H<sup>L</sup> |
 *              G<sup>R</sup>, G:H<sup>R</sup>}.</code>
 * <p>
 * Thus <code>G:H</code> is like <code>G+H</code>, except that any move in
 * <code>G</code> annihilates the copy of <code>H</code>.
 * <p>
 * Note that although <code>G:H</code> depends only on the value, and not the
 * form, of <code>H</code>, it may depend on the form of <code>G</code>.  For
 * example, <code>0:1 = 1</code>, but <code>{-1|1}:1 = 1/2</code>.
 *
 * @author  Aaron Siegel
 * @version 0.1.1
 */
public class OrdinalSumGame implements Game, Comparable
{
    private Game g, h;
    
    /**
     * Constructs a new <code>OrdinalSumGame</code> equal to <code>g:h</code>.
     *
     * @param   g The first term of this ordinal sum.
     * @param   h The second term of this ordinal sum.
     */
    public OrdinalSumGame(Game g, Game h)
    {
        this.g = g;
        this.h = h;
    }
    
    public boolean equals(Object o)
    {
        return o instanceof OrdinalSumGame &&
            g.equals(((OrdinalSumGame) o).g) &&
            h.equals(((OrdinalSumGame) o).h);
    }
    
    public int hashCode()
    {
        return 65535 * g.hashCode() + h.hashCode();
    }
    
    public int compareTo(Object o)
    {
        if (o instanceof OrdinalSumGame)
        {
            OrdinalSumGame other = (OrdinalSumGame) o;
            int cmp = Context.getActiveContext().getGameComparator().compare(g, other.g);
            if (cmp == 0)
            {
                return Context.getActiveContext().getGameComparator().compare(h, other.h);
            }
            else return cmp;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
    
    public Collection getLeftOptions()
    {
        Collection gLeftOptions = g.getLeftOptions(), hLeftOptions = h.getLeftOptions();
        List leftOptions = new ArrayList(gLeftOptions.size() + hLeftOptions.size());
        
        leftOptions.addAll(gLeftOptions);
        for (Iterator i = hLeftOptions.iterator(); i.hasNext(); )
        {
            leftOptions.add(new OrdinalSumGame(g, (Game) i.next()));
        }
        
        return leftOptions;
    }
    
    public Collection getRightOptions()
    {
        Collection gRightOptions = g.getRightOptions(), hRightOptions = h.getRightOptions();
        List rightOptions = new ArrayList(gRightOptions.size() + hRightOptions.size());
        
        rightOptions.addAll(gRightOptions);
        for (Iterator i = hRightOptions.iterator(); i.hasNext(); )
        {
            rightOptions.add(new OrdinalSumGame(g, (Game) i.next()));
        }
        
        return rightOptions;
    }
    
    public Game getInverse()
    {
        return new OrdinalSumGame(g.getInverse(), h.getInverse());
    }
    
    public boolean isShortGame()
    {
        return g.isShortGame() && h.isShortGame();
    }
    
    public CanonicalGame canonicalize() throws NotShortGameException
    {
        if (h instanceof CanonicalGame)
        {
            return CanonicalGame.fromOptions(getLeftOptions(), getRightOptions());
        }
        else
        {
            return new OrdinalSumGame(g, h.canonicalize()).canonicalize();
        }
    }
    
    public Game simplify()
    {
        Game hSimp = h.simplify();
        Game simp = h.simplifyExpression(Game.SIMPLIFY_ORDINAL_SUM_H, new Game[] {g});
        if (simp == null)
        {
            return new OrdinalSumGame(g, hSimp);
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
     * Gets the first term of this ordinal sum.
     *
     * @return The first term of this ordinal sum.
     */
    public Game getG()
    {
        return g;
    }
    
    /**
     * Gets the second term of this ordinal sum.
     *
     * @return The second term of this ordinal sum.
     */
    public Game getH()
    {
        return h;
    }
}
