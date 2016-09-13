/*
 * This file is part of Locutil2.
 * 
 * Copyright (c) 2007 Thomas King <king@informatik.uni-mannheim.de>,
 * University of Mannheim, Germany
 * 
 * All rights reserved.
 *
 * Loclib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Loclib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Loclib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.pi4.locutil;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * A geographical position.
 * 
 * @author faerber
 * @author abiskop
 * @author koelsch
 */

public class GeoPosition implements Comparable<GeoPosition>, Serializable {

	private static final long serialVersionUID = 7318226428301060903L;
	double[] coords;
	double orientation; // in degrees
	
	public GeoPosition() {
		coords = new double[] { Double.NaN, Double.NaN, Double.NaN };
		orientation = Double.NaN;
	}
    
    /**
     * Creates a new <code>GeoPosition</code> with the specified X-/Y-coordinates.
     * Altitude (Z-coordinate) is set to 0.0. Orientation is set to Double.NaN.
     * 
     * @param x
     * @param y
     */
    public GeoPosition(double x, double y) {
        coords = new double[] {x, y, 0.0};
        orientation = Double.NaN;
    }
	
	/**
	 * Creates a new <code>GeoPosition</code> with the specified coordinates.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public GeoPosition(double x, double y, double z) {
		coords = new double[] { x, y, z };
		orientation = Double.NaN;
	}
	
    /**
     * Creates an instance of <code>GeoPosition</code>.
     * 
     * @param x  x-coord
     * @param y  y-coord
     * @param z  z-coord
     * @param orientation  must be greater than or equal to 0 and less than 360
     * @throws IllegalArgumentException  if orientation is out of range
     */
	public GeoPosition(double x, double y, double z, double orientation) {
		this(x, y, z);
		if (((orientation < 0.0) || (orientation >= 360.0)) && (!Double.isNaN(orientation)))
			throw new IllegalArgumentException("Orientation must be 0 <= x < 360 degrees.");
		this.orientation = orientation; 
	}
	
	/**
	 * Creates an instance of <code>GeoPosition</code> based on a given string. The string format
	 * is "x.x y.y z.z orienation.orientation" or "x.x y.y z.z" or "x.x,y.y,z.z,orientaton.orientation" or 
	 * "x.x,y.y,z.z".
	 * 
	 * @param pos String that represents a GeoPosition
	 * @return GeoPosition object
	 */
	public static GeoPosition parse(String pos) {
		StringTokenizer st;
		if (pos.contains(",")) {
			st = new StringTokenizer(pos, ",");
		} else {
			st = new StringTokenizer(pos);
		}
		if ((st.countTokens() == 3) || (st.countTokens() == 4)) {
			double x = Double.parseDouble(st.nextToken());
			double y = Double.parseDouble(st.nextToken());
			double z = Double.parseDouble(st.nextToken());
			double orientation = Double.NaN;
			if (st.hasMoreTokens()) {
				String currentToken = (st.nextToken()).trim();
				if (! currentToken.equals("NaN")) {
					orientation = Double.parseDouble(currentToken);
				}
			}
			return new GeoPosition(x, y, z, orientation);
		} else {
			throw new IllegalArgumentException("Given string does not represent a GeoPosition.");
		}
	}
    
    /**
     * Returns the X-coordinate of this <code>GeoPosition</code> object.
     * 
     * @return  X-coordinate
     */
    public double getX() {
        return coords[0];
    }
    
    /**
     * Returns the Y-coordinate of this <code>GeoPosition</code> object.
     * 
     * @return  Y-coordinate
     */
    public double getY() {
        return coords[1];
    }
    
    /**
     * Returns the Z-coordinate of this <code>GeoPosition</code> object.
     * 
     * @return  Z-coordinate
     */
    public double getZ() {
        return coords[2];
    }
	
    /**
     * Returns the orientation
     * 
     * @return  orientation, greater than or equal to 0 and less than 360
     */
	public double getOrientation() {
		return orientation;
	}
    
    /**
     * Sets the X-coordinate of this <code>GeoPosition</code> object to the specified value.
     * 
     * @param x  the new X-coordinate
     */
	public void setX(double x) {
		coords[0] = x;
	}
    
    /**
     * Sets the Y-coordinate of this <code>GeoPosition</code> object to the specified value.
     * 
     * @param y  the new Y-coordinate
     */
    public void setY(double y) {
        coords[1] = y;
    }
    
    /**
     * Sets the Z-coordinate of this <code>GeoPosition</code> object to the specified value.
     * 
     * @param z  the new Z-coordinate
     */
    public void setZ(double z) {
        coords[2] = z;
    }
	
    /**
     * Sets the orientation to the specified value.
     * 
     * @param orientation  must be greater than or equal to 0 and less than 360
     * @throws IllegalArgumentException  if orientation is out of range
     */
	public void setOrientation(double orientation) {
		if ((orientation < 0.0) || (orientation >= 360.0))
			throw new IllegalArgumentException("Orientation must be 0 <= x < 360 degrees.");
		this.orientation = orientation;
	}
	
	/**
	 * Check if GeoPosition is initialized or not. A GeoPosition is initialized if and only if all coordinates (X, Y, Z)
	 * are not set to NaN.
	 * @return <code>true</code> if this object is initialized, <code>false</code> otherwise
	 */
	public boolean isInitialized() {
		if (Double.isNaN(coords[0]) && Double.isNaN(coords[1]) && Double.isNaN(coords[2])) return false;
		return true;
	}
	
	/**
	 * Determines whether two <code>GeoPosition</code>s are equal.
     * 
	 * @param pos2	the <code>GeoPosition</code> to compare this instance to
	 * @return	<code>true</code> if equal, <code>false</code> otherwise
	 */
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		GeoPosition pos2 = (GeoPosition) o;
		if ((Double.isNaN(coords[0])) && (Double.isNaN(coords[1])) && (Double.isNaN(coords[2])) && (Double.isNaN(pos2.coords[0])) &&
		    (Double.isNaN(pos2.coords[1])) && (Double.isNaN(pos2.coords[2])) && (Double.isNaN(orientation) && (Double.isNaN(pos2.orientation)))) {
			return true;
		}
        if ((coords[0] != pos2.coords[0]) || (coords[1] != pos2.coords[1]) || (coords[2] != pos2.coords[2]))
        	return false;
        return (orientation == pos2.orientation) || (Double.isNaN(orientation) && Double.isNaN(pos2.orientation));
	}
	
	/**
	 * Determines whether two <code>GeoPosition</code>s are equal by only considering the positions.
     * 
	 * @param pos2	the <code>GeoPosition</code> to compare this instance to
	 * @return	<code>true</code> if equal, <code>false</code> otherwise
	 */
	public boolean equalsWithoutOrientation(Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		GeoPosition pos2 = (GeoPosition) o;
        if ((coords[0] != pos2.coords[0]) || (coords[1] != pos2.coords[1]) || (coords[2] != pos2.coords[2]))
        	return false;
        return true;
	}
	
	/**
	 * Returns a hashcode that encodes the coordinates and the orientation of this <code>GeoPosition</code> object as <code>Integer</code>. The generated hash codes stay unique as long as the coordinates are integer values smaller than 128.
	 */
	public int hashCode() {
        int x = ((int) coords[0]) % 127;
        int y = ((int) coords[1]) % 127;
        int z = ((int) coords[2]) % 127;
        int bits29to23 = x << 23;
        int bits22to16 = y << 16;
        int bits15to9 = z << 9;
        int bits8to0 = ((int) orientation);
		return bits29to23 + bits22to16 + bits15to9 + bits8to0;
	}
    
    /**
     * Returns the euclidian distance between this <code>GeoPosition</code> and the argument.
     */
    public double distance(GeoPosition pos2) {
    	return Math.sqrt(
                (coords[0] - pos2.coords[0]) * (coords[0] - pos2.coords[0])
              + (coords[1] - pos2.coords[1]) * (coords[1] - pos2.coords[1]) 
              + (coords[2] - pos2.coords[2]) * (coords[2] - pos2.coords[2])); 
    }
    
    /**
	 * Adds the coordinates to the current position and returns the newly calculated location.
	 * 
	 * @param pos
	 * @return
	 */
	public GeoPosition addPosition(GeoPosition pos) {
		GeoPosition newPosition = new GeoPosition();
		newPosition.setX(getX() + pos.getX());
		newPosition.setY(getY() + pos.getY());
		newPosition.setZ(getZ() + pos.getZ());
		newPosition.setOrientation(getOrientation());
		return newPosition;
	}
    
	/**
	 * Multiplies the coordinates of a GeoPosition with a factor and returns
	 * the newly calculated location.
	 * 
	 * @param factor
	 * @return
	 */
	public GeoPosition stretch(double factor) {
		GeoPosition newPosition = new GeoPosition();
		newPosition.setX(getX() * factor);
		newPosition.setY(getY() * factor);
		newPosition.setZ(getZ() * factor);
		newPosition.setOrientation(getOrientation());
		return newPosition;
	}
	
	/**
     * Returns the coordinates and orientation of this <code>GeoPosition</code> object as <code>String</code>.
     * 
     * @return  coordinates and orientation as String
     */
    public String toString() {
    	return "(" + coords[0] + ", " + coords[1] + ", " + coords[2] + ", " + orientation + ")";
    }
    
    public String toStringWithoutOrientation() {
    	return "(" + coords[0] + ", " + coords[1] + ", " + coords[2] + ")";
    }
    
    public int compareTo(GeoPosition g) {
    	if (this.getX() < g.getX()) {
    		return -1;
    	} else if (this.getX() > g.getX()) {
    		return 1;
    	} else {
    		if (this.getY() < g.getY()) {
    			return -1;
    		} else if (this.getY() > g.getY()) {
    			return 1;
    		} else {
    			if (this.getZ() < g.getZ()) {
        			return -1;
        		} else if (this.getZ() > g.getZ()) {
        			return 1;
        		} else {
        			if (this.getOrientation() < g.getOrientation()) {
            			return -1;
            		} else if (this.getOrientation() > g.getOrientation()) {
            			return 1;
            		} else {
            			return 0;
            		}
        		}
    		}
    	}
    }
    
    public int compare(GeoPosition g1, GeoPosition g2) {
    	if (g1.getX() < g2.getX()) {
    		return -1;
    	} else if (g1.getX() > g2.getX()) {
    		return 1;
    	} else {
    		if (g1.getY() < g2.getY()) {
    			return -1;
    		} else if (g1.getY() > g2.getY()) {
    			return 1;
    		} else {
    			if (g1.getZ() < g2.getZ()) {
        			return -1;
        		} else if (g1.getZ() > g2.getZ()) {
        			return 1;
        		} else {
        			if (g1.getOrientation() < g2.getOrientation()) {
            			return -1;
            		} else if (g1.getOrientation() > g2.getOrientation()) {
            			return 1;
            		} else {
            			return 0;
            		}
        		}
    		}
    	}
    }
    
    /**
     * Returns a deep copy of the GeoPosition object
     */
    public GeoPosition clone() {
    	GeoPosition pos = new GeoPosition(getX(), getY(), getZ(), getOrientation());
    	return pos;
    }
}
