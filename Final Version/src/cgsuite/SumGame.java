/*
 * SumGame.java
 *
 * Created on October 21, 2002, 11:40 PM
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
 * The sum of an arbitrary number of games.  The components of the
 * sum can be any objects of type {@link Game}.
 *
 * @author  Aaron Siegel
 * @version 0.1.1
 */
public final class SumGame implements Game, Comparable
{
    private Game[] components;

    private SumGame()
    {
    }
    
    /**
     * Constructs a new <code>SumGame</code> with the specified components.
     *
     * @param   components The components of this sum.  Every component must
     *          be an instance of <code>{@link Game}</code>.
     */
    public SumGame(Collection components)
    {
        this.components = new Game[components.size()];
        components.toArray(this.components);
    }

    /**
     * Constructs a new <code>SumGame</code> with exactly two components.
     *
     * @param   g The first component of this sum.
     * @param   h The second component of this sum.
     */
    public SumGame(Game g, Game h)
    {
        components = new Game[2];
        components[0] = g;
        components[1] = h;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SumGame))
        {
            return false;
        }
        
        SumGame h = (SumGame) o;
        if (components.length != h.components.length)
        {
            return false;
        }
        
        for (int i = 0; i < components.length; i++)
        {
            if (!components[i].equals(h.components[i]))
            {
                return false;
            }
        }
        
        return true;
    }
    
    public int hashCode()
    {
        int hc = 1;
        for (int i = 0; i < components.length; i++)
        {
            hc = 31 * hc + components[i].hashCode();
        }
        return hc;
    }
    
    public int compareTo(Object o)
    {
        if (!(o instanceof SumGame))
        {
            throw new IllegalArgumentException();
        }
        
        SumGame h = (SumGame) o;
        for (int i = 0; i < Math.min(components.length, h.components.length); i++)
        {
            int cmp = Context.getActiveContext().getGameComparator().compare(components[i], h.components[i]);
            if (cmp != 0) return cmp;
        }
        
        return components.length - h.components.length;
    }
    
    public Collection getLeftOptions()
    {
        return getOptions(true);
    }
    
    public Collection getRightOptions()
    {
        return getOptions(false);
    }
    
    private Collection getOptions(boolean left)
    {
        List options = new LinkedList();
        for (int i = 0; i < components.length; i++)
        {
            if (i > 0 && components[i].equals(components[i-1]))
            {
                // Can skip this one
                continue;
            }
            for (Iterator j = left ? components[i].getLeftOptions().iterator() :
                                     components[i].getRightOptions().iterator(); j.hasNext();)
            {
                SumGame option = new SumGame();
                option.components = (Game[]) components.clone();
                option.components[i] = (Game) j.next();
                options.add(option);
            }
        }
        return options;
    }
    
    public Game getInverse()
    {
        SumGame g = new SumGame();
        g.components = new Game[components.length];
        for (int i = 0; i < components.length; i++)
        {
            g.components[i] = components[i].getInverse();
        }
        return g;
    }
    
    public boolean isShortGame()
    {
        for (int i = 0; i < components.length; i++)
        {
            if (!components[i].isShortGame())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the canonical form of this game.  First, each component of
     * this sum is canonicalized, and then the results are added using
     * {@link CanonicalGame#plus(CanonicalGame) CanonicalGame.plus}.
     * 
     * @return  The canonical form of this game.
     * @throws  NotShortGameException One of the components of the sum is not
     *          a short game.
     */
    public CanonicalGame canonicalize() throws NotShortGameException
    {
        CanonicalGame g = CanonicalGame.ZERO;
        for (int i = 0; i < components.length; i++)
        {
            g = g.plus(components[i].canonicalize());
        }
        return g;
    }

    public Game simplify()
    {
        List newComponents = new ArrayList();
        for (int i = 0; i < components.length; i++)
        {
            Game simp = components[i].simplify();
            if (simp instanceof SumGame)
            {
                newComponents.addAll(((SumGame) simp).getComponents());
            }
            else
            {
                newComponents.add(simp);
            }
        }

        int firstUntested = 0;
        do
        {
            firstUntested = simplifyComponentList(newComponents, firstUntested);
        } while (firstUntested < newComponents.size());
        
        Collections.sort(newComponents, Context.getActiveContext().getGameComparator());
        
        if (newComponents.size() == 0)
        {
            return CanonicalGame.ZERO;
        }
        else if (newComponents.size() == 1)
        {
            return (Game) newComponents.get(0);
        }
        else
        {
            return new SumGame(newComponents);
        }
    }
    
    private static int simplifyComponentList(List components, int firstUntested)
    {
        int nextFU = components.size();
        for (int i = 0; i < nextFU; i++)
        {
            for (int j = Math.max(firstUntested, i+1); j < nextFU; j++)
            {
                if (i == j) continue;
                Game g = (Game) components.get(i);
                Game h = (Game) components.get(j);
                Game simp = g.simplifyExpression(Game.SIMPLIFY_SUM, new Game[] {h});
                if (simp == null)
                {
                    simp = h.simplifyExpression(Game.SIMPLIFY_SUM, new Game[] {g});
                }
                if (simp != null)
                {
                    components.remove(j);
                    components.remove(i);
                    if (simp instanceof SumGame)
                    {
                        components.addAll(((SumGame) simp).getComponents());
                    }
                    else
                    {
                        components.add(simp);
                    }
                    i--;
                    nextFU -= 2;
                    break;
                }
            }
        }
        
        return nextFU;
    }
    
    public Game simplifyExpression(int simplifyType, Game[] args)
    {
        Game[] newComponents;
        
        switch (simplifyType)
        {
            case Game.SIMPLIFY_PRODUCT_G:
                newComponents = new Game[components.length];
                for (int i = 0; i < components.length; i++)
                {
                    newComponents[i] = new ProductGame(components[i], args[0]).simplify();
                }
                return new SumGame(Arrays.asList(newComponents)).simplify();
                
            case Game.SIMPLIFY_PRODUCT_U:
                if (args[0] instanceof CanonicalGame &&
                    ((CanonicalGame) args[0]).isInteger())
                {
                    newComponents = new Game[components.length];
                    for (int i = 0; i < components.length; i++)
                    {
                        newComponents[i] =
                            new ProductGame(args[0], components[i]).simplify();
                    }
                    return new SumGame(Arrays.asList(newComponents)).simplify();
                }
                else
                {
                    return null;
                }
                
            default:
                return null;
        }
    }

    /**
     * Gets the components of this sum.
     *
     * @return  A <code>List</code> containing this sum's components.
     */
    public List getComponents()
    {
        return Collections.unmodifiableList(Arrays.asList(components));
    }

    public String toString()
    {
        if (components.length == 0)
        {
            return "0";
        }
        else
        {
            String s = components[0].toString();
            for (int i = 1; i < components.length; i++)
            {
                s += " + " + components[i].toString();
            }
            return s;
        }
    }
    
}
