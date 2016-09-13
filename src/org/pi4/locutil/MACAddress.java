/*
 * This file is part of Locutil1.
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
import java.util.*;

/**
 * This class represents a MAC Address
 * 
 * @author king
 * @author faerber
 */
public class MACAddress implements Comparable<MACAddress>, Serializable {
	
	private static final long serialVersionUID = -8815095335593354738L;

	public static MACAddress parse(String string) {
		MACAddress ba = new MACAddress();
		StringTokenizer st = new StringTokenizer(string, ":");
		if (st.countTokens() != 6) throw new IllegalArgumentException("Invalid format.");
		int counter = 0;
		while (st.hasMoreTokens()) {
			String temp = st.nextToken();
			if (temp.length() != 2) throw new IllegalArgumentException("Invalid format.");
			ba.macAddress[counter++] = Short.parseShort(temp, 16);
		}
		return ba;
	}
	
	private short[] macAddress;
	
	protected MACAddress() {
		this.macAddress = new short[6];
	}
	
	public MACAddress(short[] mac) {
		this.macAddress = mac;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(17);
		for (int i = 0; i < 6; ++i) {
			if (i != 0) sb.append(':');
			if (macAddress[i] < 16)
				sb.append('0');
			sb.append(Integer.toHexString(macAddress[i]));
		}
		return sb.toString().toUpperCase();
	}
	
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass()) return false;
		MACAddress ba = (MACAddress) o;
		for (int i = 0; i < 6; i++) {
			if (macAddress[i] != ba.macAddress[i])
				return false;
		}
		return true;
	}
	
	public int hashCode() {
		// NOTE: This algorithm is designed to ignore the first two components of the MAC.
		int hash = 0;
		for (int i = 0; i < 4; i++) {
			hash |= macAddress[2+i] << 8*(3-i);
		}
		return hash;
	}
	
	public int compareTo(MACAddress o) {	
		MACAddress mac2 = (MACAddress)o;
		for (int i = 0; i < macAddress.length; i++) {
			if (macAddress[i] > mac2.macAddress[i]) {
				return 1;
			} else if (macAddress[i] < mac2.macAddress[i]) {
				return -1;
			}
		}
		return 0;
	}
	
	public int compare(MACAddress o1, MACAddress o2) {
		MACAddress mac1 = (MACAddress)o1;
		MACAddress mac2 = (MACAddress)o2;
		for (int i = 0; i < mac1.macAddress.length; i++) {
			if (mac1.macAddress[i] > mac2.macAddress[i]) {
				return 1;
			} else if (mac1.macAddress[i] < mac2.macAddress[i]) {
				return -1;
			}
		}
		return 0;
	}
    
    /**
     * Returns a deep copy of this <code>MACAddress</code> object. 
     */
    public Object clone() {
        short[] macCopy = new short[6];
        for (int i = 0; i < 6; i++) {
            macCopy[i] = macAddress[i];
        }
        return new MACAddress(macCopy);
    }
}
