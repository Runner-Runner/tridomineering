/*
 * Thermograph.java
 *
 * Created on October 22, 2002, 10:00 PM
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
 * A thermograph.  (Code and documentation will ultimately be revised.)
 *
 * @author  Aaron Siegel
 * @version 0.1.1
 */
public class Thermograph
{
    /**
     * A single thermograph trajectory.  (Code and documentation will
     * ultimately be revised.)
     */
    public static class Trajectory
    {
        public DyadicRational mast;
        public DyadicRational[] criticalTemps;
        
        public Trajectory()
        {
            mast = null;
            criticalTemps = new DyadicRational[0];
        }
        
        public String toString()
        {
            String s = "(M = " + mast.toString() + ")";
            for (int i = 0; i < criticalTemps.length; i++)
            {
                s += " " + criticalTemps[i].toString();
            }
            return s;
        }

        public DyadicRational displacementAt(DyadicRational temp)
        {
            DyadicRational displacement = DyadicRational.ZERO;
            
            int i;
            for (i = 1; i < criticalTemps.length && temp.compareTo(criticalTemps[i]) <= 0; i += 2)
            {
                displacement = displacement.plus(criticalTemps[i-1]).minus(criticalTemps[i]);
            }
            if (i-1 < criticalTemps.length && temp.compareTo(criticalTemps[i-1]) < 0)
            {
                displacement = displacement.plus(criticalTemps[i-1]).minus(temp);
            }
            
            return displacement;
        }
        
        public DyadicRational temperature()
        {
            if (criticalTemps.length == 0)
            {
                return DyadicRational.ZERO;
            }
            else
            {
                return criticalTemps[0];
            }
        }
        
        public DyadicRational getCriticalTemp(int index)
        {
            if (index == criticalTemps.length)
            {
                return new DyadicRational(-1, 1);
            }
            else
            {
                return criticalTemps[index];
            }
        }
    }
    
    public DyadicRational waterLevel;
    public Trajectory leftTrajectory, rightTrajectory;
    
    public DyadicRational leftValueAt(DyadicRational temp)
    {
        if (temp.compareTo(waterLevel) < 0 || temp.isInfinite())
        {
            throw new UnsupportedOperationException();
        }
        return leftTrajectory.mast.plus(leftTrajectory.displacementAt(temp));
    }
    
    public DyadicRational rightValueAt(DyadicRational temp)
    {
        if (temp.compareTo(waterLevel) < 0 || temp.isInfinite())
        {
            throw new UnsupportedOperationException();
        }
        return rightTrajectory.mast.minus(rightTrajectory.displacementAt(temp));
    }

    public DyadicRational getTemperature()
    {
        return DyadicRational.max
            (leftTrajectory.temperature(), rightTrajectory.temperature());
    }
    
    public DyadicRational getMast()
    {
        return leftTrajectory.mast;
    }
    
    public String toString ()
    {
        String s = "Thermograph: LT = " + leftTrajectory.toString() +
            "; RT = " + rightTrajectory.toString();
        
        return s;
    }
    
    public Thermograph()
    {
        waterLevel = new DyadicRational(-1, 1);
        leftTrajectory = new Trajectory();
        rightTrajectory = new Trajectory();
    }
}
