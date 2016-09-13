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

package org.pi4.locutil.trace.orientationfilter;

import java.util.HashSet;
import java.util.Iterator;

/**
 * This class filters orientations and is supposed to be used in the tracegenerator.
 * A list of well-known orientations have to be stored inside this class and only trace entries
 * with these orientations will be selected.
 * 
 * @author king
 */
public class OrientationFilter {
	HashSet<Double> orientations;
	
	public OrientationFilter() {
		orientations = new HashSet<Double>();
	}
	
	public void add(Double orientation) {
		orientations.add(orientation);
	}
	
	public boolean contains(Double orientation) {
		return orientations.contains(orientation);
	}
		
	public String toString() {
		if (orientations.isEmpty()) {
			return new String("Empty");
		}
		StringBuffer sb = new StringBuffer();
		Iterator<Double> it = orientations.iterator();
		while (it.hasNext()) {
			Double orientation = it.next();
			sb.append(orientation);
			if (it.hasNext())
				sb.append(", ");
		}
		return sb.toString();
	}
	
	public boolean isEmpty() {
		return orientations.isEmpty();
	}
}
