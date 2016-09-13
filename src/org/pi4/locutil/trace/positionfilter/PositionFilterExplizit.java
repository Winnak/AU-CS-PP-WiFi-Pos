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
 * PositionFilterExplizit builds a set of points based on given positions. This class is supposed
 * to be used for the online as well as the offline set.
 * 
 * @author king
 *
 */
public class PositionFilterExplizit implements PositionFilter {
	HashSet<GeoPosition> explizitPositions;
	
	public PositionFilterExplizit() {
		explizitPositions = new HashSet<GeoPosition>();
	}
	
	public void add(GeoPosition pos) {
		explizitPositions.add(pos);
	}
	
	public boolean contains(GeoPosition pos) {
		return explizitPositions.contains(pos);
	}
	
	public boolean isEmpty() {
		return explizitPositions.isEmpty();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("PositionFilterExplizit: ");
		if (isEmpty()) {
			sb.append("Emptry");
		} else {
			Iterator<GeoPosition> it = explizitPositions.iterator();
			while (it.hasNext()) {
				sb.append(it.next());
				if (it.hasNext())
					sb.append(", ");
			}
		}
		return sb.toString();
	}
}
