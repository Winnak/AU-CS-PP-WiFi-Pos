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

public class PositioningError implements Comparable<PositioningError> {
	private GeoPosition realPosition;
	private GeoPosition estimatedPosition;
	
	public PositioningError(GeoPosition realPosition, GeoPosition estimatedPosition) {
		this.realPosition = realPosition;
		this.estimatedPosition = estimatedPosition;
	}
	
	public GeoPosition getRealPosition() {
		return realPosition;
	}
	
	public GeoPosition getEstimatedPosition() {
		return estimatedPosition;
	}
	
	public double getPositioningError() {
		return realPosition.distance(estimatedPosition);
	}
	
	public int compareTo(PositioningError pe) {
		if (pe.getPositioningError() > getPositioningError()) return -1;
		if (pe.getPositioningError() < getPositioningError()) return 1;
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (realPosition.equals(((PositioningError) o).realPosition) && estimatedPosition.equals(((PositioningError) o).estimatedPosition))
			return true;
		return false;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Positioning error: ");
		sb.append(realPosition);
		sb.append(' ');
		sb.append(estimatedPosition);
		sb.append(": ");
		sb.append(getPositioningError());
		return sb.toString();
	}
}
