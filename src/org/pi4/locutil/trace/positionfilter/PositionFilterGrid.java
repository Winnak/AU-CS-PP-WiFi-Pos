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

import org.pi4.locutil.GeoPosition;

/**
 * PositionFilterGrid builds a grid based on a start position and a grid dimension. This class is supposed to
 * be used only for offline sets!
 * 
 * @author king
 *
 */
public class PositionFilterGrid implements PositionFilter {
	double gridDimension = -1;
	GeoPosition startPos = new GeoPosition();

	public boolean contains(GeoPosition pos) {
		if (isEmpty()) return true;
		else {
			if (((Math.abs(pos.getX() - startPos.getX()) % gridDimension) == 0) &&
				((Math.abs(pos.getY() - startPos.getY()) % gridDimension) == 0)	&&
				((Math.abs(pos.getZ() - startPos.getZ()) % gridDimension) == 0)) return true;
		}
		return false;
	}

	public boolean isEmpty() {
		if (startPos.equals(new GeoPosition()) && (gridDimension == -1)) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("PositionFilterGrid: ");
		if (isEmpty()) {
			sb.append("Empty");
		} else {
			sb.append("GridDimension: ");
			sb.append(gridDimension);
			sb.append(" StartPos: ");
			sb.append(startPos);
		}
		return sb.toString();
	}
	
	public void setGridDimension(double gridDimension) {
		this.gridDimension = gridDimension;
	}
	
	public void setStartPosition(GeoPosition startPos) {
		this.startPos = startPos;
	}
	
	public GeoPosition getStartPosition() {
		return startPos;
	}
}
