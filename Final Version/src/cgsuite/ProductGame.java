/*
 * ProductGame.java
 *
 * Created on October 27, 2002, 12:18 AM
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
 * The Norton product of two games.  If <code>G</code> is an integer, then
 * <code>G.U</code> is simply equal to the sum of <code>G</code> copies of
 * <code>U</code>.  Otherwise, it is equal to the Norton product of
 * <code>G</code> by <code>U</code> as defined in Winning Ways.
 * <p>
 * In particular, <code>G.</code>&uarr; yields a game of atomic weight
 * <code>G</code>, for any <code>G</code>.
 * <p>
 * Note that <code>G.U</code> will not behave as expected unless
 * <code>G</code> is an integer <i>or</i> <code>U > 0</code>.
 *
 * @author  Aaron Siegel
 * @version 0.1.1
 */
public class ProductGame implements Game, Comparable
{
    private Game g, u;
    
    /**
     * Constructs a new <code>ProductGame</code> with the specified game and
     * unit.
     *
     * @param   g The first term of this product.
     * @param   u The unit of this product.
     */
    public ProductGame(Game g, Game u)
    {
        this.g = g;
        this.u = u;
    }
    
    public boolean equals(Object o)
    {
        return o instanceof ProductGame &&
            g.equals(((ProductGame) o).g) &&
            u.equals(((ProductGame) o).u);
    }
    
    public int hashCode()
    {
        return 65535 * g.hashCode() + u.hashCode();
    }
    
    public int compareTo(Object o)
    {
        if (o instanceof ProductGame)
        {
            ProductGame other = (ProductGame) o;
            int cmp = Context.getActiveContext().getGameComparator().compare(g, other.g);
            if (cmp == 0)
            {
                return Context.getActiveContext().getGameComparator().compare(u, other.u);
            }
            else return cmp;
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
        return new ProductGame(g.getInverse(), u);
    }
    
    public boolean isShortGame()
    {
        return g.isShortGame() && u.isShortGame();
    }
    
    public CanonicalGame canonicalize() throws NotShortGameException
    {
        return g.canonicalize().nortonProduct(u.canonicalize());
    }

    public Game simplify()
    {
        Game gSimp = g.simplify(), uSimp = u.simplify();
        Game simp = gSimp.simplifyExpression(Game.SIMPLIFY_PRODUCT_G, new Game[] {uSimp});
        if (simp == null)
        {
            simp = uSimp.simplifyExpression(Game.SIMPLIFY_PRODUCT_U, new Game[] {gSimp});
        }
        if (simp == null)
        {
            return new ProductGame(gSimp, uSimp);
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
     * Gets the first term of this product.
     *
     * @return  The first term of this product.
     */
    public Game getG()
    {
        return g;
    }
    
    /**
     * Gets the unit of this product.
     *
     * @return The unit of this product.
     */
    public Game getU()
    {
        return u;
    }
    
    public String toString()
    {
        return g.toString() + ".(" + u.toString() + ")";
    }
}
