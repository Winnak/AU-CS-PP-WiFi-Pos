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

package org.pi4.locutil.trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.MACAddress;
import org.pi4.locutil.trace.macfilter.MacFilter;
import org.pi4.locutil.trace.macfilter.MacFilterExplizit;
import org.pi4.locutil.trace.orientationfilter.OrientationFilter;
import org.pi4.locutil.trace.positionfilter.PositionFilter;
import org.pi4.locutil.trace.positionfilter.PositionFilterAllowAll;

/**
 * Parser.
 * 
 * @author koelsch
 * @author faerber
 * @author king
 * @author lubberger
 */
public class Parser {

	double precision;
	private File file;
	private MacFilter mf;
	private OrientationFilter of;
	private PositionFilter psf;
	private boolean skipAdHocNodes = true;


	public Parser(File file) {
		if (file == null)
			throw new IllegalArgumentException("file cannot be null");
		this.file = file;
		mf = new MacFilterExplizit();
		of = new OrientationFilter();
		psf = new PositionFilterAllowAll();
	}
	
	public void setSkipAdHocNodes(boolean skip) {
		skipAdHocNodes = skip;
	}
	
	public boolean getSkipAdHocNodes() {
		return skipAdHocNodes;
	}
	
	public void setMacFilter(MacFilter mf) {
		this.mf = mf;
	}
	
	public MacFilter getMacFilter() {
		return mf;
	}
	
	public void setOrientationFilter(OrientationFilter of) {
		this.of = of;
	}
	
	public OrientationFilter getOrientationFilter() {
		return of;
	}
		
	public void resetOrientationFilter() {
		of = new OrientationFilter();
	}
	
	public void resetMacFilter() {
		mf = new MacFilterExplizit();
	}
	
	public void setPositionFilter(PositionFilter psf) {
		this.psf = psf;
	}
	
	public PositionFilter getPositionFilter() {
		return this.psf;
	}
	
	public void resetPositionFilter() {
		this.psf = new PositionFilterAllowAll();
	}
	
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	
	public ArrayList<TraceEntry> parse() throws IOException, NumberFormatException {
				
		ArrayList<TraceEntry> list = new ArrayList<TraceEntry>();
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		
		try {
			// Process each line.
			while ((line = in.readLine()) != null) {
				boolean skip = false;
				if (line.startsWith("#"))
					continue;
				
				TraceEntry entry = new TraceEntry();		
				String[] components = line.split(";");
				for (String s: components) {
					String[] kv = s.split("=", 2);
					if (kv.length != 2)
						throw new IOException("Expected key=value, found: " + s);
					String key = kv[0];
					String value = kv[1];
					if (key.equals("t")) {
						entry.setTimestamp(Long.parseLong(value));
					} else if (key.equals("id")) {
						entry.setId(MACAddress.parse(value));
					} else if (key.equals("pos")) {
						String[] coords = value.split(",", 3);
						if (coords.length != 3)
							throw new IOException("Expected x, y, z coordinates, found: " + value);
						entry.setGeoPosition(new GeoPosition(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2])));
						if (!psf.isEmpty()) {
							if (! psf.contains(entry.getGeoPosition())) {
								skip = true;
							}
						}
					} else if (key.equals("degree")) {
						double degree = Math.round(Double.parseDouble(value) / precision) * precision;
						if (degree == 360) degree = 0;
						if (!of.isEmpty()) {
							if (!of.contains(degree)) continue;
						}
						entry.getGeoPosition().setOrientation(degree); // assume position comes first
					} else if (key.equals("speed")) {
						double speed = Double.parseDouble(value);
						entry.setSpeed(speed);
					} else { // assume it is a MAC to SSI mapping
						try {
							MACAddress mac = MACAddress.parse(key);
							if (!mf.isEmpty()) {
								if (!mf.contains(mac)) continue;
							}
							String[] parts = value.split(",");
							// skip ad-hoc-nodes
							if (skipAdHocNodes && ((parts.length == 3) || (parts.length == 4))) {
								if (parts[2].equals("1"))
									continue;
							}
							if ((parts.length == 2) || (parts.length == 3)) {
								// put mac, signalstrength, and channel
								entry.getSignalStrengthSamples().put(mac, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
							} else if (parts.length == 4) {
								// put mac, signalstrength, noise, and channel
								entry.getSignalStrengthSamples().put(mac, Double.parseDouble(parts[0]), Double.parseDouble(parts[3]), Double.parseDouble(parts[1]));
							}
						} catch (IllegalArgumentException ex) {
							System.err.println(s);
							throw new IOException("Expected MAC address");
						}
					}
				}
				if (skip) continue;
				list.add(entry);
			}
		} finally {
			in.close();
		}
		return list;
	}
}
