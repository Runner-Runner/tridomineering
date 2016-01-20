/*
 * Game.java
 *
 * Created on October 15, 2002, 4:46 PM
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
 * The primary interface for combinatorial games in Combinatorial Game Suite.
 * Every type of game
 * must implement <code>Game</code>.  If you are implementing a short game (that
 * is, a loopfree game with finitely many subpositions), you should consider
 * deriving a class from {@link AbstractShortGame} instead.
 * <p>
 * <i>Every instance of <code>Game</code> must be immutable.</i>  The
 * internal state of a <code>Game</code> can change (for example, to update
 * a cache), but not in a way that is visible to other components.
 *
 * @author  Aaron Siegel
 * @version 0.1.1
 */
public interface Game extends java.io.Serializable
{
    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_SUM = 1;

    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_PRODUCT_G = 2;

    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_PRODUCT_U = 3;

    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_ORDINAL_SUM_G = 4;

    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_ORDINAL_SUM_H = 5;

    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_COOL = 6;

    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_FREEZE = 7;

    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_HEAT = 8;

    /**
     * For use by {@link #simplifyExpression(int, Game[]) simplifyExpression}.
     * For advanced users only.
     */
    public final static int SIMPLIFY_OVERHEAT = 9;
    
    /**
     * Gets a <code>Collection</code> containing all left options of this game.
     *
     * @return  A <code>Collection</code> containing all left options of this
     *          game.
     */
    Collection getLeftOptions();
    
    /**
     * Gets a <code>Collection</code> containing all right options of this game.
     *
     * @return  A <code>Collection</code> containing all right options of this
     *          game.
     */
    Collection getRightOptions();
    
    /**
     * Gets the inverse of this game.
     *
     * @return  The inverse of this game.
     */
    Game getInverse(); 
    
    /**
     * Returns <code>true</code> if this game is a short game.
     *
     * @return  <code>true</code> if this game is a short game.
     */
    boolean isShortGame();

    /**
     * Calculates the canonical form of this game.  The canonical form is the
     * unique {@link CanonicalGame} that is canonically equal to this game.
     *
     * @return  The canonical form of this game.
     * @see     #simplify() simplify
     * @throws  NotShortGameException This game is not a short game (and
     *          therefore has no corresponding <code>CanonicalGame</code>.)
     */
    CanonicalGame canonicalize() throws NotShortGameException;
    
    /**
     * Calculates a simplified form of this game.  The simplified form need not
     * be canonical or "simplest."  This method is used by the Combinatorial
     * Game Suite interface to determine the best way to display output.  As
     * an example, the {@link ExplicitGame} given by
     * <code>{1,1/2|-1}</code> would be simplified to the
     * {@link CanonicalGame} given by <code>{1|-1}</code>.
     * <p>
     * There are two key differences between <code>simplify</code> and
     * {@link #canonicalize() canonicalize}.  First, <code>canonicalize</code>
     * <i>always</i> returns a <code>CanonicalGame</code>, while
     * <code>simplify</code> may return any game at all.  Second,
     * <code>simplify</code> is called automatically by the Combinatorial
     * Game Suite user interface when output is ready to be displayed, while
     * <code>canonicalize</code> is usually called only by explicit user
     * request.  For this reason, it is advisable for <code>simplify</code>
     * to execute rapidly in most circumstances.
     * <p>
     * It is permissible for <code>simplify</code> to return <code>this</code>
     * when no obvious simplifications are possible, but it should
     * <i>never</i> return <code>null</code>.
     *
     * @return  A simplified form of this game.
     * @see     #canonicalize() canonicalize
     */
    Game simplify();
    
    /**
     * Calculates a simplified form of the indicated expression.  This method
     * is for advanced users and is rarely needed by plug-ins.  Most
     * implementations of <code>Game</code> should simply return
     * <code>null</code>.
     *
     * @param   simplifyType The type of simplification to be performed.
     * @param   args The simplification arguments.
     * @return  A simplified form of the indicated expression.
     */
    Game simplifyExpression(int simplifyType, Game[] args);
}
