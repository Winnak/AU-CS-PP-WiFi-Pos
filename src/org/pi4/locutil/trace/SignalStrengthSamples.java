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

import java.util.*;

import org.pi4.locutil.MACAddress;
import org.pi4.locutil.Statistics;

/**
 * Recorded signal strength samples.
 * 
 * @author faerber
 * @author king
 */
public class SignalStrengthSamples {
	
	private HashMap<MACAddress, SignalStrengthNoiseAndChannel> samples;
	private long timestamp;
	
	public SignalStrengthSamples() {
		samples = new HashMap<MACAddress, SignalStrengthNoiseAndChannel>();
		timestamp = -1;
	}
	
	public SignalStrengthSamples(SignalStrengthSamples samples) {
		this.samples = new HashMap<MACAddress, SignalStrengthNoiseAndChannel>(samples.samples);
		timestamp = -1;
	}
	
	public void add(SignalStrengthSamples add) {
		Iterator<MACAddress> it = add.keySet().iterator();
		while (it.hasNext()) {
			MACAddress mac = it.next();
			Iterator<Double> it2 = add.iterator(mac);
			while (it2.hasNext()) {
				Double sample = it2.next();
				put(mac, sample);
			}
		}
	}
	
	public void put(MACAddress mac, double signalStrength) {
		if (samples.containsKey(mac)) {
			samples.get(mac).addSignalStrength(signalStrength);
		} else {
			samples.put(mac, new SignalStrengthNoiseAndChannel(signalStrength, Double.NaN));
		}
	}
	
	public void put(MACAddress mac, double signalStrength, double channel) {
		if (samples.containsKey(mac)) {
			samples.get(mac).addSignalStrength(signalStrength);
		} else {
			samples.put(mac, new SignalStrengthNoiseAndChannel(signalStrength, channel));
		}
	}
	
	public void put(MACAddress mac, double signalStrength, double noise, double channel) {
		if (samples.containsKey(mac)) {
			samples.get(mac).addSignalStrength(signalStrength);
			samples.get(mac).addNoise(noise);
		} else {
			samples.put(mac, new SignalStrengthNoiseAndChannel(signalStrength, noise, channel));
		}
	}
	
	public SignalStrengthSamples getSignalStrengthSamples(MACAddress mac) {
		SignalStrengthSamples sss = new SignalStrengthSamples();
		sss.samples.put(mac, samples.get(mac));
		return sss;
	}
	
	public Vector<Double> getSignalStrengthValues(MACAddress currentMac) {
		return new Vector<Double>(samples.get(currentMac).getSignalStrengthSamples());
	}
	
	public Vector<Double> getNoiseValues(MACAddress currentMac) {
		return new Vector<Double>(samples.get(currentMac).getNoiseSamples());
	}
		
	public double getFirstSignalStrength(MACAddress mac) {
		return samples.get(mac).getFirstSignalStrength();
	}
	
	public double getFirstNoiseValue(MACAddress mac) {
		return samples.get(mac).getFirstNoiseValue();
	}
	
	public double getAverageSignalStrength(MACAddress mac) {
		return samples.get(mac).getAverageSignalStrength();
	}
	
	public double getStandardDeviationSignalStrength(MACAddress mac) {
		return samples.get(mac).getStandardDeviationSignalStrength();
	}
	
	public double getVarianceSignalStrength(MACAddress mac) {
		return samples.get(mac).getVarianceSignalStrength();
	}
	
	public double getAverageNoise(MACAddress mac) {
		return samples.get(mac).getAverageNoise();
	}
	
	public Iterator<Double> iterator(MACAddress mac) {
		return samples.get(mac).getSignalStrengthSamples().iterator();
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
	 * Sorts the access points of a FingerprintDatabaseEntry according to the
	 * average signal strength in descending order. 
	 * 
	 * @return sorted list of access points
	 */
	public LinkedList<MACAddress> getSortedAccessPoints() {
		AverageSignalStrengthComparator c = new AverageSignalStrengthComparator();
		LinkedList<MACAddress> sortedList = new LinkedList<MACAddress>(samples.keySet());
		// use comparator to sort the list
		Collections.sort(sortedList, c);
		return sortedList;
	}
	
	public double getChannel(MACAddress mac) {
		return samples.get(mac).getChannel();
	}
	
	public boolean containsKey(MACAddress mac) {
		return samples.containsKey(mac);
	}
	
	public boolean containsKeys(Set<MACAddress> mac) {
		for (MACAddress addr: mac) {
			if (!containsKey(addr)) return false; 
		}
		return true;
	}
	
	public boolean containsKeys(ArrayList<MACAddress> macs) {
		Iterator<MACAddress> iterMacs = macs.iterator();
		while (iterMacs.hasNext()) {
			MACAddress mac = iterMacs.next();
			if (!containsKey(mac)) return false;
		}
		return true;
	}
	
	public int getCount(MACAddress mac) {
		return samples.get(mac).getSignalStrengthSamples().size();
	}
	
	public Set<MACAddress> keySet() {
		return samples.keySet();
	}
	
	public void remove(MACAddress mac) {
		samples.remove(mac);
	}
	
	public int size() {
		return samples.size();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator<MACAddress> sampleIterator = samples.keySet().iterator();
		MACAddress macAddress;
		while (sampleIterator.hasNext()) {
			macAddress = sampleIterator.next();
			Iterator<Double> ssIterator = samples.get(macAddress).getSignalStrengthSamples().iterator();
			Iterator<Double> noiseIterator = samples.get(macAddress).getNoiseSamples().iterator();
			while (ssIterator.hasNext() || noiseIterator.hasNext()) {
				if (ssIterator.hasNext()) {
					sb.append(";");
					sb.append(macAddress);
					sb.append("=");
					sb.append(ssIterator.next());
					sb.append(",");
					sb.append(samples.get(macAddress).getChannel());
				}
				if (noiseIterator.hasNext()) {
					sb.append(",");
					sb.append(noiseIterator.next());
				}
			}
		}
		return sb.toString();
	}
	
	class SignalStrengthNoiseAndChannel {
		private Vector<Double> signalStrength;
		private Vector<Double> noise;
		private double channel;
		
		private boolean unchangedSignalAvg = false;
		private boolean unchangedSignalVar = false;
		private boolean unchangedSignalStdDev = false;
		private double signalAvg = 0.0;
		private double signalVar = 0.0;
		private double signalStdDev = 0.0;
		
		private boolean unchangedNoiseAvg = false;
		private double noiseAvg = 0.0;
		
		public SignalStrengthNoiseAndChannel() {
			signalStrength = new Vector<Double>();
			noise = new Vector<Double>();
		}
		
		public SignalStrengthNoiseAndChannel(double signalStrength, double channel) {
			this.signalStrength = new Vector<Double>();
			this.noise = new Vector<Double>();
			this.signalStrength.add(signalStrength);
			this.channel = channel;
			unchangedSignalAvg = false;
			unchangedSignalStdDev = false;
			unchangedNoiseAvg = false;
		}
		
		public SignalStrengthNoiseAndChannel(double signalStrength, double noise, double channel) {
			this.signalStrength = new Vector<Double>();
			this.noise = new Vector<Double>();
			this.signalStrength.add(signalStrength);
			this.noise.add(noise);
			this.channel = channel;
			unchangedSignalAvg = false;
			unchangedSignalStdDev = false;
			unchangedNoiseAvg = false;
		}
		
		public double getFirstSignalStrength() {
			return signalStrength.firstElement();
		}
		
		public double getFirstNoiseValue() {
			return noise.firstElement();
		}
		
		public double getAverageSignalStrength() {
			if (unchangedSignalAvg) return signalAvg;
			signalAvg = Statistics.avg(signalStrength);
			unchangedSignalAvg = true;
			return signalAvg;
		}
		
		public double getVarianceSignalStrength() {
			if (unchangedSignalVar) return signalVar;
			signalVar = Statistics.var(signalStrength);
			unchangedSignalVar = true;
			return signalVar;
		}
		
		public double getStandardDeviationSignalStrength() {
			if (unchangedSignalStdDev) return signalStdDev;
			signalStdDev = Statistics.stdDev(signalStrength);
			unchangedSignalStdDev = true;
			return signalStdDev;
		}
		
		public Vector<Double> getSignalStrengthSamples() {
			return signalStrength;
		}
		
		public Vector<Double> getNoiseSamples() {
			return noise;
		}
		
		public double getAverageNoise() {
			if (unchangedNoiseAvg) return noiseAvg;
			noiseAvg = Statistics.avg(noise);
			unchangedNoiseAvg = true;
			return noiseAvg;
		}
		
		public double getChannel() {
			return channel;
		}
		
		public void addSignalStrength(double signalStrength) {
			this.signalStrength.add(signalStrength);
			unchangedSignalAvg = false;
			unchangedSignalStdDev = false;
		}
		
		public void addNoise(double noise) {
			this.noise.add(noise);
		}
		
		public void setChannel(int channel) {
			this.channel = channel;
		}
	}
	
	/**
	 * Compares the average signal strength of two accesspoints. It is used 
	 * to sort the TreeMap in descending order.
	 */
	private class AverageSignalStrengthComparator implements Comparator<MACAddress> {
		/**
		 * This method uses the mac id´s to compare the value components of the TreeMap,
		 * NOT the keys.
		 * 
		 * @param o1 access point id
		 * @param o2 access point id
		 * @return comparison value
		 */
		public int compare(MACAddress o1, MACAddress o2) {
			if ((o1 == null) || (o2 == null))
				throw new IllegalArgumentException("The arguments cannot be null.");
			double avg1 = samples.get(o1).getAverageSignalStrength();
			double avg2 = samples.get(o2).getAverageSignalStrength();
			return Double.compare(avg2, avg1);	
		}
	}
}
