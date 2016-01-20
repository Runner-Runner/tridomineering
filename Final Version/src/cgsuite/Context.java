/*
 * Context.java
 *
 * Created on March 17, 2003, 12:08 PM
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

import java.util.Comparator;
import java.util.Map;

/**
 * Contains utility methods for communicating with the Combinatorial
 * Game Suite interface.  Plug-ins can use this class to reference the
 * primary cache, generate output in mid-calculation, report warning
 * and debugging messages, and otherwise communicate with the kernel.
 * <p>
 * Plug-in writers should call the static method
 * {@link #getActiveContext() getActiveContext} to obtain a reference
 * to the <code>Context</code> object owned by the kernel.  For
 * example, to obtain a reference to the primary cache, use:
 * <p>
 * <code>Context.getActiveContext().getPrimaryCache()</code>
 * <p>
 * Plug-ins should <i>never</i> call
 * {@link #setActiveContext(Context) setActiveContext}.  It should only
 * be called by stand-alone applications that use the CGSuite
 * library and need additional control over cache management or
 * output display.
 */
public abstract class Context
{
    static Context activeContext = new DefaultContext();
 
    /**
     * Gets a reference to the primary cache.  The <i>primary cache</i>
     * is a hashtable that is shared between the CGSuite core library and
     * all plug-ins.  Plug-ins should prefer to use the primary cache rather
     * than a private cache; this gives the kernel greater control over
     * memory management.
     *
     * @return  A reference to the primary cache.
     */
    public abstract Map getPrimaryCache();
    
    /**
     * Gets a <code>Comparator</code> that can be used to compare any two
     * objects of type {@link Game}.
     * <p>
     * This is useful primarily for games that have many other objects of
     * type <code>Game</code> as sub-games.  The comparator can be used to
     * conduct more efficient searches and to provide consistent output.
     *
     * @return  A <code>Comparator</code> that can be used to compare any
     *          two games.
     */
    public abstract Comparator getGameComparator();
    
    /**
     * Creates a string representation of the specified object.  An output
     * handler for the object must be registered with the kernel (see the
     * {@link cgsuite.plugin} package for details.)
     * <p>
     * Note that a game's <code>toString</code> method is often
     * insufficient for this purpose.  For example, the <code>String</code>
     * that corresponds to a {@link CanonicalGame} depends on user-specific
     * options.
     * 
     * @param   obj The object to convert to a <code>String</code>.
     * @return  A string representation of <code>obj</code>.
     */
    public abstract String createOutputString(Object obj);
    
    /**
     * Generates output for the specified object.  The output is added to
     * the kernel's output queue to be displayed at the next opportunity.
     * <p>
     * This method should be used by plug-ins that need to display output
     * in mid-calculation.
     *
     * @param   obj The object to output.
     */
    public abstract void generateOutput(Object obj);
    
    /**
     * Generates a warning message.
     * <p>
     * This method should be used by plug-ins to report a warning - that is,
     * an error that is not severe enough to justify aborting the calculation.
     *
     * @param   warning The warning message to display.
     */
    public abstract void generateWarning(String warning);
    
    public abstract void generateLogMessage(int level, String source, String message);
    
    public abstract void checkKernelState();

    /**
     * Gets the <code>Context</code> used to communicate with the kernel.
     * Plug-ins should call this method to access the primary cache, display
     * output and warnings, and otherwise access the CGSuite interface.
     *
     * @return  The active context.
     */
    public static Context getActiveContext()
    {
        return activeContext;
    }
    
    /**
     * Sets the active context.
     * <p>
     * <b>Warning to plug-in writers:</b> Plug-ins should <i>not</i> call this
     * method.  It should only be called by stand-alone applications that use
     * the CGSuite library and need additional control over cache management or
     * output display.
     */
    public static void setActiveContext(Context context)
    {
        activeContext = context;
    }

    public static class DefaultContext extends Context
    {
        Map primaryCache = new java.util.HashMap(1023);
        Comparator gameComparator = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                if (!(o1 instanceof Game && o2 instanceof Game))
                {
                    throw new IllegalArgumentException();
                }
                if (o1.getClass() == o2.getClass())
                {
                    if (Comparable.class.isAssignableFrom(o1.getClass()))
                    {
                        return ((Comparable) o1).compareTo(o2);
                    }
                    else
                    {
                        return o1.hashCode() - o2.hashCode();
                    }
                }
                else
                {
                    return o1.getClass().hashCode() - o2.getClass().hashCode();
                }
            }
        };
        
        public Map getPrimaryCache()
        {
            return primaryCache;
        }
        
        public Comparator getGameComparator()
        {
            return gameComparator;
        }
        
        public String createOutputString(Object obj)
        {
            return obj.toString();
        }
        
        public void generateOutput(Object obj)
        {
            System.out.println(obj);
        }
        
        public void generateWarning(String warning)
        {
            System.out.println(warning);
        }
        
        public void generateLogMessage(int level, String source, String message)
        {
            System.out.println("[" + source + "] " + message);
        }
        
        public void checkKernelState()
        {
        }
    }
}
