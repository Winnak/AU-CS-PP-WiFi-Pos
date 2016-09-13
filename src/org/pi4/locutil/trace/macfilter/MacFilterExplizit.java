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

package org.pi4.locutil.trace.macfilter;

import java.util.*;

import org.pi4.locutil.MACAddress;

/**
 * This class filters mac addresses from access points accidentily stored in the trace file.
 * A list of well-known access points have to be stored inside this class and only this access
 * points will be returned by the parser (all unlisted access points will be removed).
 * 
 * @author king
 */
public class MacFilterExplizit implements MacFilter {
	protected HashSet<MACAddress> macs;
	
	public MacFilterExplizit() {
		macs = new HashSet<MACAddress>();
	}
	
	public void add(MACAddress mac) {
		macs.add(mac);
	}
	
	public boolean contains(MACAddress mac) {
		return macs.contains(mac);
	}
	
	public HashSet<MACAddress> getMacs() {
		return macs;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("MacFilterExplizit: ");
		if (macs.isEmpty()) {
			sb.append("Empty");
		} else {
			Iterator<MACAddress> it = macs.iterator();
			while (it.hasNext()) {
				MACAddress mac = it.next();
				sb.append(mac);
				if (it.hasNext())
					sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	public boolean isEmpty() {
		return macs.isEmpty();
	}
}
