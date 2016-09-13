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

package org.pi4.locutil.trace.positionfilter;

import java.util.HashSet;
import java.util.Iterator;

import org.pi4.locutil.GeoPosition;

/**
 * PositionFilterExclude excludes given positions from a set of points. This class is supposed
 * to be used for the online as well as the offline set.
 * 
 * @author king
 *
 */
public class PositionFilterExclude implements PositionFilter {
	HashSet<GeoPosition> excludedPositions;
	
	public PositionFilterExclude() {
		excludedPositions = new HashSet<GeoPosition>();
	}
	
	public void add(GeoPosition pos) {
		excludedPositions.add(pos);
	}

	public boolean contains(GeoPosition pos) {
		return !excludedPositions.contains(pos);
	}
	
	public boolean isEmpty() {
		return excludedPositions.isEmpty();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("PositionFilterExclude: ");
		if (isEmpty()) {
			sb.append("Empty");
		} else {
			Iterator<GeoPosition> it = excludedPositions.iterator();
			while (it.hasNext()) {
				sb.append(it.next());
				if (it.hasNext())
					sb.append(", ");
			}
		}
		return sb.toString();
	}
}
