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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.pi4.locutil.MACAddress;

public class MacFilterOr implements MacFilter {
	Vector<MacFilter> macFilters;
	
	public MacFilterOr() {
		macFilters = new Vector<MacFilter>();
	}
	
	public void add(MacFilter mf) {
		macFilters.add(mf);
	}
	
	public boolean contains(MACAddress mac) {
		boolean contains = false;
		for (MacFilter mf : macFilters) {
			if (!contains) contains = mf.contains(mac);
		}
		return contains;
	}
	
	public boolean isEmpty() {
		if (macFilters.size() > 0) return false;
		return true;
	}
	
	public String toString() {
		StringBuffer sf = new StringBuffer();
		sf.append("MacFilterOr:");
		sf.append(' ');
		for (MacFilter mf : macFilters) {
			sf.append(mf);
			sf.append(' ');
		}
		return sf.toString();
	}
	
	public Set<MACAddress> getMacs() {
		HashSet<MACAddress> macs = new HashSet<MACAddress>();
		for (MacFilter mf : macFilters) {
			macs.addAll(mf.getMacs());
		}
		return macs;
	}
	
	public void add(MACAddress mac) {
		throw new IllegalStateException("A MAC address cannot be added to MacFilterOr.");
	}
}
