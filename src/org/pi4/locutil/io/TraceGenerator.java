package org.pi4.locutil.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.MACAddress;
import org.pi4.locutil.Random;
import org.pi4.locutil.trace.Parser;
import org.pi4.locutil.trace.SignalStrengthSamples;
import org.pi4.locutil.trace.TraceEntry;

/**
 * Utility class used to generate offline and online sets for various positioning
 * algorithms.
 *
 * @author lubberger
 */
public class TraceGenerator {
	private ArrayList<TraceEntry> offlineTraceEntries;
	private ArrayList<TraceEntry> onlineTraceEntries;
	private ArrayList<TraceEntry> offlineSet;
	private ArrayList<TraceEntry> onlineSet;
	private ArrayList<MACAddress> minimumMacSet;
	private ArrayList<Integer> onlineSetBuckets;
	private ArrayList<GeoPosition> trackingPos;
	private Hashtable<GeoPosition, ArrayList<TraceEntry>> offlineTraceEntryBuckets;
	private Hashtable<GeoPosition, ArrayList<TraceEntry>> onlineTraceEntryBuckets;
	
	private boolean verbose = false;
	private boolean warnings = true;
	private boolean discardOrientationInFingerprints;
	private int offlineSize;
	private int onlineSize;
	private double onlineRatio;
	private String traceType;
	
	// Constructor
	public TraceGenerator(Parser offlineParser, Parser onlineParser, int offlineSize, int onlineSize) throws IOException, NumberFormatException {
		verbose = false;
		warnings = false;
		if (verbose) System.out.println("TraceGenerator created.");
		
		// set the properties
		this.offlineSize = offlineSize; 
		this.onlineSize = onlineSize;
		onlineRatio = 100.0;
		discardOrientationInFingerprints = true;
		traceType = "Rice";
		onlineParser.setPrecision(5.0);
		offlineParser.setPrecision(45.0);
		offlineTraceEntries = new ArrayList<TraceEntry>(offlineParser.parse());
		onlineTraceEntries = new ArrayList<TraceEntry>(onlineParser.parse());
		
		if (verbose) System.out.println("TraceGenerator was fed with " + offlineTraceEntries.size() + " TraceEntries for the offline set and " + onlineTraceEntries.size() + " TraceEntries for the online set.");
	}
	
	public int getOnlineSetSize() {
		return onlineSize;
	}
	
	public int getOfflineSetSize() {
		return offlineSize;
	}
	
	public String getTraceType() {
		return traceType;
	}
	
	// Method for generating the offline and online sets
	public void generate() throws RuntimeException {
		// required to keep track of the order of the online set traceentries (used by tracking)
		trackingPos = new ArrayList<GeoPosition>();
		
		// Check if a traceType has been specified.
		if (traceType.equals(""))
			throw new RuntimeException("No traceType specified!");
		// Sort the traceEntries into different buckets
		// according to their positions and orientations.
		if (offlineTraceEntries.size() == 0)
			throw new RuntimeException("Cannot generate the offline set without any traceEntries!");
		if (onlineTraceEntries.size() == 0)
			throw new RuntimeException("Cannot generate the online set without any traceEntries!");
				
		// initiate offline trace data structures
		offlineTraceEntryBuckets = new Hashtable<GeoPosition, ArrayList<TraceEntry>>();
		// initiate online trace data structures
		onlineTraceEntryBuckets = new Hashtable<GeoPosition, ArrayList<TraceEntry>>();
				
		for (int i = 0; i < offlineTraceEntries.size(); i++) {
			TraceEntry te = offlineTraceEntries.get(i);
			GeoPosition gp = te.getGeoPosition();
			if (discardOrientationInFingerprints)
				gp.setOrientation(Double.NaN);
			
			if (offlineTraceEntryBuckets.containsKey(gp)) { // a bucket for this position and orientation already exists
				offlineTraceEntryBuckets.get(gp).add(te);
			} else { // no bucket yet for this position and orientation
				ArrayList<TraceEntry> traceEntryBucket = new ArrayList<TraceEntry>();
				traceEntryBucket.add(te);
				offlineTraceEntryBuckets.put(gp, traceEntryBucket);
			}
		}		
		if (verbose) System.out.println("TraceGenerator: Data for " + offlineTraceEntryBuckets.size() + " different fingerprints found.");
		
		for (int i = 0; i < onlineTraceEntries.size(); i++) {
			TraceEntry te = onlineTraceEntries.get(i);
			GeoPosition gp = te.getGeoPosition();
			if (discardOrientationInFingerprints)
				gp.setOrientation(Double.NaN);
			
			if (onlineTraceEntryBuckets.containsKey(gp)) { // a bucket for this position and orientation already exists
				onlineTraceEntryBuckets.get(gp).add(te);
			} else { // no bucket yet for this position and orientation
				ArrayList<TraceEntry> traceEntryBucket = new ArrayList<TraceEntry>();
				traceEntryBucket.add(te);
				onlineTraceEntryBuckets.put(gp, traceEntryBucket);
			}
			if (!trackingPos.contains(gp)) trackingPos.add(gp);
		}
		if (verbose) System.out.println("TraceGenerator: Data for " + onlineTraceEntryBuckets.size() + " different test positions found.");
		
		// Operations depending on the chosen traceType:
		if (traceType.equals("Radar")) {
			// Determine the minimum MAC set.
			if (verbose) System.out.println("TraceGenerator: Determining the minimum MAC set of all fingerprints and test positions ...");
			minimumMacSet = determineMinimumMacSet();

			// Delete all traceEntries that don't contain all the MAC addresses
			// in the minimum MAC set.
			if (verbose) System.out.println("TraceGenerator: Deleting traceEntries that don't contain all the MAC addresses in the minimum MAC set ...");
			deleteUnsuitableTraceEntries(0, offlineTraceEntryBuckets);
			deleteUnsuitableTraceEntries(0, onlineTraceEntryBuckets);
			
			// Delete all MAC addresses not contained in the minimum MAC set from
			// all the traceEntries.
			if (verbose) System.out.println("TraceGenerator: Trimming the remaining traceEntries to match the minimum MAC set ...");
			trimTraceEntries(0, offlineTraceEntryBuckets);
			trimTraceEntries(0, onlineTraceEntryBuckets);

			// Check if the buckets still contain enough entries.
			checkBucketSizes();
			
			// Generate the sets.
			generateSets();
		} else if (traceType.equals("RadarPUnknown")) {
			// Check if the buckets still contain enough entries.
			checkBucketSizes();
			
			// Generate the sets.
			generateSets();
		} else if (traceType.equals("Rice")) {
			// Check if the buckets contain enough entries.
			checkBucketSizes();

			// Generate the sets.
			generateSets();
		} else {
			throw new RuntimeException("Unknown traceType!");
		}
		if (verbose) System.out.println("TraceGenerator: Done.");
	}
	
	// Methods for getting private fields
	public ArrayList<TraceEntry> getOffline() {
		return new ArrayList<TraceEntry>(offlineSet);
	}
	
	public ArrayList<TraceEntry> getOnline() {
		return new ArrayList<TraceEntry>(onlineSet);
	}
	
	// Methods for writing the generated sets to files.
	public void writeOffline(File outfile) throws IOException {
		PrintStream out = new PrintStream(outfile);
		
		// Iterate through the offline set.
		for (int i = 0; i < offlineSet.size(); i++) {
			TraceEntry te = offlineSet.get(i);
			String line = "t=" + te.getTimestamp() + ";id=" + te.getId() + ";pos=";
			GeoPosition gp = te.getGeoPosition();
			line += gp.getX() + "," + gp.getY() + "," + gp.getZ() + ";degree=" + te.getGeoPosition().getOrientation();
			SignalStrengthSamples samples = te.getSignalStrengthSamples();
			Iterator<MACAddress> sampleIterator = samples.keySet().iterator();
			while (sampleIterator.hasNext()) {
				MACAddress macAddress = sampleIterator.next();
				line += ";" + macAddress.toString() + "=" + samples.getFirstSignalStrength(macAddress) + "," + samples.getChannel(macAddress);
			}
			out.println(line);
			out.flush();
		}
		// Close the output file.
		out.close();
	}
	
	public void writeOnline(File outfile) throws IOException {
		PrintStream out = new PrintStream(outfile);
		
		// Iterate through the online set.
		for (int i = 0; i < onlineSet.size(); i++) {
			TraceEntry te = onlineSet.get(i);
			String line = "t=" + te.getTimestamp() + ";id=" + te.getId() + ";pos=";
			GeoPosition gp = te.getGeoPosition();
			line += gp.getX() + "," + gp.getY() + "," + gp.getZ() + ";degree=" + te.getGeoPosition().getOrientation();
			SignalStrengthSamples samples = te.getSignalStrengthSamples();
			Iterator<MACAddress> sampleIterator = samples.keySet().iterator();
			while (sampleIterator.hasNext()) {
				MACAddress macAddress = sampleIterator.next();
				line += ";" + macAddress.toString() + "=" + samples.getFirstSignalStrength(macAddress) + "," + samples.getChannel(macAddress);
			}
			out.println(line);
			out.flush();
		}
		// Close the output file.
		out.close();
	}

	private ArrayList<MACAddress> determineMinimumMacSet() {
		// Determine the minimum MAC-Set (MAC-addresses contained in every bucket).
		Hashtable<MACAddress, Boolean> minimumMacSet = new Hashtable<MACAddress, Boolean>();
		boolean first = true;
		
		// iterate through all the buckets
		Iterator<GeoPosition> bucketIterator = offlineTraceEntryBuckets.keySet().iterator();
		while (bucketIterator.hasNext()) {
			GeoPosition key = bucketIterator.next();
			ArrayList<TraceEntry> traceEntryBucket = offlineTraceEntryBuckets.get(key);
			// iterate through all the entries in the current bucket
			for (int i = 0; i < traceEntryBucket.size(); i++) {
				// iterate through all the samples in the current entry
				Iterator<MACAddress> sampleIterator = traceEntryBucket.get(i).getSignalStrengthSamples().keySet().iterator();
				while (sampleIterator.hasNext()) {
					MACAddress macAddress = sampleIterator.next();
					if (first) { // only in the first bucket:
						if (!minimumMacSet.containsKey(macAddress))
							minimumMacSet.put(macAddress, false);
					} else { // in all the other buckets:
						if (minimumMacSet.containsKey(macAddress)) // this address is still in the minimum MAC-Set
							minimumMacSet.put(macAddress, true); // mark the address as found (in this bucket)						
					}
				}
			}
			if (first) {
				first = false;
			} else {
				// remove every address that wasn't found in the current bucket from the set
				ArrayList<MACAddress> addressesToRemove = new ArrayList<MACAddress>();
				Iterator<MACAddress> macIterator = minimumMacSet.keySet().iterator();
				while (macIterator.hasNext()) {
					MACAddress macAddress = macIterator.next();
					if (!minimumMacSet.get(macAddress)) {
						if (!addressesToRemove.contains(macAddress))
							addressesToRemove.add(macAddress);
					} else {
						// reset the "search result" for the next bucket
						minimumMacSet.put(macAddress, false);
					}
				}
				for (int i = 0; i < addressesToRemove.size(); i++)
					minimumMacSet.remove(addressesToRemove.get(i));
			}
		}
		// again for the online set ...
		bucketIterator = onlineTraceEntryBuckets.keySet().iterator();
		while (bucketIterator.hasNext()) {
			GeoPosition key = bucketIterator.next();
			ArrayList<TraceEntry> traceEntryBucket = onlineTraceEntryBuckets.get(key);
			// iterate through all the entries in the current bucket
			for (int i = 0; i < traceEntryBucket.size(); i++) {
				// iterate through all the samples in the current entry
				Iterator<MACAddress> sampleIterator = traceEntryBucket.get(i).getSignalStrengthSamples().keySet().iterator();
				while (sampleIterator.hasNext()) {
					MACAddress macAddress = sampleIterator.next();
					if (minimumMacSet.containsKey(macAddress)) // this address is still in the minimum MAC-Set
						minimumMacSet.put(macAddress, true); // mark the address as found (in this bucket)						
				}
			}
			// remove every address that wasn't found in the current bucket from the set
			ArrayList<MACAddress> addressesToRemove = new ArrayList<MACAddress>();
			Iterator<MACAddress> macIterator = minimumMacSet.keySet().iterator();
			while (macIterator.hasNext()) {
				MACAddress macAddress = macIterator.next();
				if (!minimumMacSet.get(macAddress)) {
					if (!addressesToRemove.contains(macAddress))
						addressesToRemove.add(macAddress);
				} else {
					// reset the "search result" for the next bucket
					minimumMacSet.put(macAddress, false);
				}
			}
			for (int i = 0; i < addressesToRemove.size(); i++)
				minimumMacSet.remove(addressesToRemove.get(i));
		}
		
		// create the array list
		ArrayList<MACAddress> mms = new ArrayList<MACAddress>();
		Iterator<MACAddress> macIterator = minimumMacSet.keySet().iterator();
		while (macIterator.hasNext()) {
			mms.add(macIterator.next());
		}
		if (verbose) {
			System.out.print("TraceGenerator: Minimum MAC set: ");
			for (int i = 0; i < mms.size(); i++) {
				if (i > 0) System.out.print(", ");
				System.out.print(mms.get(i).toString());
			}
			System.out.print("\n");
		}
		return mms;
	}
	
	private void deleteUnsuitableTraceEntries(int p, Hashtable<GeoPosition, ArrayList<TraceEntry>> traceEntryBuckets) {
		Iterator<GeoPosition> bucketIterator = traceEntryBuckets.keySet().iterator();
		
		// Iterate through all the buckets.
		while (bucketIterator.hasNext()) {
			GeoPosition key = bucketIterator.next();
			ArrayList<TraceEntry> traceEntryBucket = traceEntryBuckets.get(key);
			// Iterate through all the entries in the current bucket.
			for (int i = 0; i < traceEntryBucket.size(); i++) {
				// Iterate through the minimum MAC set and count the MAC addresses
				// that are not contained in the current traceEntry.
				int missingMACs = 0;
				for (int j = 0; j < minimumMacSet.size(); j++) {
					if (!traceEntryBucket.get(i).getSignalStrengthSamples().containsKey(minimumMacSet.get(j)))
						missingMACs++;
				}
				// Delete the traceEntry if more than p MACs are missing.
				if (missingMACs > p) {
					traceEntryBucket.remove(i);
					i--; // the next entry is now at index i, not i+1
				}
			}
		}
	}
	
	private void trimTraceEntries(int p, Hashtable<GeoPosition, ArrayList<TraceEntry>> traceEntryBuckets) {
		Iterator<GeoPosition> bucketIterator = traceEntryBuckets.keySet().iterator();
		
		// Iterate through all the buckets.
		while (bucketIterator.hasNext()) {
			GeoPosition key = bucketIterator.next();
			ArrayList<TraceEntry> traceEntryBucket = traceEntryBuckets.get(key);
			// Iterate through all the entries in the current bucket.
			for (int i = 0; i < traceEntryBucket.size(); i++) {
				// Iterate through all the samples in the current entry and count
				// the additional MACs (not contained in the minimum MAC set).
				ArrayList<MACAddress> additionalMACs = new ArrayList<MACAddress>();
				SignalStrengthSamples samples = traceEntryBucket.get(i).getSignalStrengthSamples();
				Iterator<MACAddress> sampleIterator = samples.keySet().iterator();
				while (sampleIterator.hasNext()) {
					MACAddress macAddress = sampleIterator.next();
					if (!minimumMacSet.contains(macAddress)) { // additional address?
						if (!additionalMACs.contains(macAddress)) { // new additional address? 
							additionalMACs.add(macAddress);
						}
					}
				}
				// Delete (additionalMACs.size - p) MAC addresses at random.
				int macsToDelete = additionalMACs.size() - p;
				for (int j = 0; j < macsToDelete; j++) {
					// Get an index between 0 (incl.) and the current
					// number of additional MACs (excl.).
					int k = Random.nextInt(additionalMACs.size()); 
					// Get the corresponding MAC address.
					MACAddress macAddress = additionalMACs.get(k);
					// Delete the MAC address from the list of additional MACs and
					// the traceEntry.
					additionalMACs.remove(macAddress);
					samples.remove(macAddress);
				}
			}
		}
	}
	
	private void chooseOnlineSetBuckets() {
		// Every position should only be contained once in the online set.
		// So let's first find out how many positions there are by sorting the
		// index numbers of the traceEntryBuckets into positionBuckets.
		Hashtable<GeoPosition, ArrayList<Integer>> positionBuckets = new Hashtable<GeoPosition, ArrayList<Integer>>();
		// iterate through the onlineTraceEntryBuckets
		Iterator<GeoPosition> bucketIterator = onlineTraceEntryBuckets.keySet().iterator();
		int j = 0;
		while (bucketIterator.hasNext()) {
			GeoPosition key = bucketIterator.next();
			GeoPosition positionKey = new GeoPosition(key.getX(), key.getY(), key.getZ());
			if (positionBuckets.containsKey(positionKey)) { // the bucket for this position already exists
			 	positionBuckets.get(positionKey).add(j);
			} else { // there's no bucket for this position yet
				ArrayList<Integer> indexList = new ArrayList<Integer>();
				indexList.add(j);
				positionBuckets.put(positionKey, indexList);
			}
			j++;
		}
		// Transform the positionBucket-Hashtable into an ArrayList
		ArrayList<ArrayList<Integer>> positionArray = new ArrayList<ArrayList<Integer>>();
		bucketIterator = positionBuckets.keySet().iterator();
		while (bucketIterator.hasNext()) {
			positionArray.add(positionBuckets.get(bucketIterator.next()));
		}
		// Determine the number of positions to use in the online set.
		long onlineSetPositionCount = Math.round(positionArray.size() * onlineRatio / 100.);
		if (onlineSetPositionCount == 0)
			onlineSetPositionCount = 1;
		// Create a list from 0 to the number of positions - 1
		ArrayList<Integer> positionIndexList = new ArrayList<Integer>();
		for (int i = 0; i < positionArray.size(); i++)
			positionIndexList.add(i);
		// Draw onlineSetPositionCount distinct index numbers at random.
		onlineSetBuckets = new ArrayList<Integer>();
		for (int i = 0; i < onlineSetPositionCount; i++) {
			int pi = Random.nextInt(positionIndexList.size());
			// from the chosen position, choose one bucket ...
			int bi = Random.nextInt(positionArray.get(positionIndexList.get(pi)).size()); 
			onlineSetBuckets.add(positionArray.get(positionIndexList.get(pi)).get(bi));
			// ... and delete the position from the position list
			positionIndexList.remove(pi);
		}
		if (verbose) System.out.println("TraceGenerator: The online set will contain " + onlineSetBuckets.size() + " positions.");
	}
	
	private void checkBucketSizes() {
		Iterator<GeoPosition> bucketIterator = offlineTraceEntryBuckets.keySet().iterator();
		while (bucketIterator.hasNext()) {
			GeoPosition key = bucketIterator.next();
			int i = offlineTraceEntryBuckets.get(key).size();
			if (i < offlineSize) {
				throw new RuntimeException("Not enough traceEntries (" + i + ") for position " + key.getX() + "/" + key.getY() + "/" + key.getZ() + " and orientation " + key.getOrientation() + " to build the offline set!");
			} else {
				if (i == offlineSize)
					if (warnings) System.out.println("TraceGenerator WARNING: All traceEntries for position " + key.getX() + "/" + key.getY() + "/" + key.getZ() + " and orientation " + key.getOrientation() + " in the offline tracefile need to be used. Randomization is impossible!");
			}
		}
		bucketIterator = onlineTraceEntryBuckets.keySet().iterator();
		while (bucketIterator.hasNext()) {
			GeoPosition key = bucketIterator.next();
			int i = onlineTraceEntryBuckets.get(key).size();
			if (i < onlineSize) {
				throw new RuntimeException("Not enough traceEntries (" + i + ") for position " + key.getX() + "/" + key.getY() + "/" + key.getZ() + " and orientation " + key.getOrientation() + " to build the online set!");
			} else {
				if (i == onlineSize)
					if (warnings) System.out.println("TraceGenerator WARNING: All traceEntries for position " + key.getX() + "/" + key.getY() + "/" + key.getZ() + " and orientation " + key.getOrientation() + " in the online tracefile need to be used. Randomization is impossible!");
			}
		}
	}
	
	private void generateSets() {	
		// Generate the online set.
		chooseOnlineSetBuckets();
		if (verbose) System.out.println("TraceGenerator: Generating the online set ...");
		onlineSet = new ArrayList<TraceEntry>();
		// Iterate through all the buckets.
		int bucketCount = 0;
		//Iterator<GeoPosition> bucketIterator = onlineTraceEntryBuckets.keySet().iterator();
		Iterator<GeoPosition> iterTrackingPos = trackingPos.iterator();
		while (iterTrackingPos.hasNext()) {
			GeoPosition key = iterTrackingPos.next();
			ArrayList<TraceEntry> traceEntryBucket = onlineTraceEntryBuckets.get(key);
			// If the current bucket was chosen to contribute to the online set,
			// copy onlineSize of the entries (at random) into the online set and
			// delete them from the bucket.
			if (onlineSetBuckets.contains(bucketCount)) {
				for (int i = 0; i < onlineSize; i++) {
					// Get an index between 0 (incl.) and the size of the bucket (excl.).
					int j = Random.nextInt(traceEntryBucket.size());
					// Add the entry to the offline set.
					onlineSet.add(traceEntryBucket.get(j));
					// Delete the entry from the bucket.
					traceEntryBucket.remove(j);
				}
			}
			bucketCount++;
		}
			
		// Generate the offline set.
		if (verbose) System.out.println("TraceGenerator: Generating the offline set ...");
		offlineSet = new ArrayList<TraceEntry>();
		Iterator<GeoPosition> bucketIterator = offlineTraceEntryBuckets.keySet().iterator();
		while (bucketIterator.hasNext()) {
			GeoPosition key = bucketIterator.next();
			ArrayList<TraceEntry> traceEntryBucket = offlineTraceEntryBuckets.get(key);
			// Copy offlineSize of the entries (at random) into the
			// offline set and delete them from the bucket.
			for (int i = 0; i < offlineSize; i++) {
				// Get an index between 0 (incl.) and the size of the bucket (excl.).
				int j = Random.nextInt(traceEntryBucket.size());
				// Add the entry to the offline set.
				offlineSet.add(traceEntryBucket.get(j));
				// Delete the entry from the bucket.
				traceEntryBucket.remove(j);
			}
		}
	}
}
