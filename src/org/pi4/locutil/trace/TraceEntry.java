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

import java.io.IOException;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.MACAddress;

/**
 * Data entry.
 * 
 * @author faerber
 * @author king
 */
public class TraceEntry {
	private long timestamp;
	private GeoPosition position;
	private double speed;
	private MACAddress id;
	private SignalStrengthSamples samples;
	
	/**
	 * Creates a new data entry object.
	 * 
	 * @param timestamp	time of recording
	 * @param position	the position
	 * @param speed	the speed
	 * @param id	MAC of recording device
	 * @param samples	signal strength samples
	 * @param channel	wireless lan channel of AP
	 */
	public TraceEntry(long timestamp, GeoPosition position, double speed, MACAddress id, SignalStrengthSamples samples) {
		this.timestamp = timestamp;
		this.position = position;
		this.speed = speed;
		this.id = id;
		this.samples = samples;
	}
	
	/**
	 * Creates a new data entry object.
	 * 
	 * @param timestamp	time of recording
	 * @param position	the position
	 * @param id	MAC of recording device
	 * @param samples	signal strength samples
	 * @param channel	wireless lan channel of AP
	 */
	public TraceEntry(long timestamp, GeoPosition position, MACAddress id, SignalStrengthSamples samples) {
		this.timestamp = timestamp;
		this.position = position;
		this.speed = Double.NaN;
		this.id = id;
		this.samples = samples;
	}
	
	/**
	 * Creates an empty data entry object.
	 */
	public TraceEntry() {
		this.position = new GeoPosition();
		this.speed = Double.NaN;
		this.timestamp = -1;
		this.id = new MACAddress(new short[]{0, 0, 0, 0, 0, 0});
		this.samples = new SignalStrengthSamples();
	}
	
	/**
	 * Returns the timestamp.
	 * 
	 * @return	the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Sets the timestamp
	 * 
	 * @param timestamp	the timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Returns the position.
	 * 
	 * @return	the position
	 */
	public GeoPosition getGeoPosition() {
		return position;
	}
	
	/**
	 * Sets the position.
	 * 
	 * @param position	the position
	 */
	public void setGeoPosition(GeoPosition position) {
		this.position = position;
	}
	
	/**
	 * Returns the speed.
	 * 
	 * @return	the speed
	 */
	public double getSpeed() {
		return speed;
	}
	
	/**
	 * Sets the speed.
	 * 
	 * @param speed	the speed
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	/**
	 * Returns the MAC of the network card that has been used to sample the data.
	 * 
	 * @return	the MAC
	 */
	public MACAddress getId() {
		return id;
	}
	
	/**
	 * Sets the MAC of the network card that has been used to sample the data.
	 * 
	 * @param id	the MAC
	 */
	public void setId(MACAddress id) {
		this.id = id;
	}
	
	/**
	 * Returns the signal strength samples.
	 * 
	 * @return	the samples
	 */
	public SignalStrengthSamples getSignalStrengthSamples() {
		return samples;
	}
	
	public TraceEntry clone() {
		TraceEntry returnme = new TraceEntry(this.getTimestamp(), this.getGeoPosition(), this.getId(), this.getSignalStrengthSamples());
		return returnme;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("t=");
		sb.append(getTimestamp());
		sb.append(";id=");
		sb.append(getId());
		sb.append(";pos=");
		sb.append(getGeoPosition().getX());
		sb.append(",");
		sb.append(getGeoPosition().getY());
		sb.append(",");
		sb.append(getGeoPosition().getZ());
		sb.append(";degree=");
		sb.append(getGeoPosition().getOrientation());
		sb.append(samples);
		return sb.toString();
	}
	
	/**
	 * This function parses a string representation of a trace entry and then returns the entry
	 * 
	 * This function parses a string representation of a trace entry and then returns the entry.
	 * It ignores all settings given to any filters. The entry contains all the data that was
	 * present in the string.
	 * 
	 * @param s The string representation of a trace entry.
	 * @return the corresponding trace entry
	 * @throws IOException
	 */	
	public static TraceEntry fromString(String s) throws IOException {
		TraceEntry entry = new TraceEntry();		
		String[] components = s.split(";");
		for (String c: components) {
			String[] kv = c.split("=", 2);
			if (kv.length != 2)
				throw new IOException("Expected key=value, found: " + c);
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
			} else if (key.equals("degree")) {
				double degree = Double.parseDouble(value);
				entry.getGeoPosition().setOrientation(degree); // assume position comes first
			} else if (key.equals("speed")) {
				double speed = Double.parseDouble(value);
				entry.setSpeed(speed);
			} else { // assume it is a MAC to SSI mapping
				try {
					MACAddress mac = MACAddress.parse(key);
					String[] parts = value.split(",");
					// skip ad-hoc-nodes
					if ((parts.length == 2) || (parts.length == 3)) {
						// put mac, signalstrength, and channel
						entry.getSignalStrengthSamples().put(mac, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
					} else if (parts.length == 4) {
						// put mac, signalstrength, noise, and channel
						entry.getSignalStrengthSamples().put(mac, Double.parseDouble(parts[0]), Double.parseDouble(parts[3]), Double.parseDouble(parts[1]));
					}
				} catch (IllegalArgumentException ex) {
					System.err.println(c);
					throw new IOException("Error while parsing trace entry");
				}
			}
		}
		return entry;
	}
}
