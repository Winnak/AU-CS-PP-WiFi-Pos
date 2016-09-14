package group4pp;

import java.awt.image.SampleModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.io.TraceGenerator;
import org.pi4.locutil.trace.*;

/**
 * @author 
 *
 */
public class PrintingSSDValues
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
    	String accessPointPath = "data/MU.AP__170012_1.positions";
    	
    	// Construct parser for APs
    	File accessPointFile = new File(accessPointPath);
    	ArrayList<AccessPoint> list = LocUtility.parseAccessPointFile(accessPointFile);
    	ArrayList<ArrayList<String>> ssD = new ArrayList<ArrayList<String>>();
    	
        String offlinePath = "data/MU.1.5meters.offline.trace";
        String onlinePath = "data/MU.1.5meters.online.trace";

        // Construct parsers
        File offlineFile = new File(offlinePath);
        Parser offlineParser = new Parser(offlineFile);

        File onlineFile = new File(onlinePath);
        Parser onlineParser = new Parser(onlineFile);

        // Construct trace generator
        TraceGenerator tg;
        try
        {
            int offlineSize = 25;
            int onlineSize = 5;
            tg = new TraceGenerator(offlineParser, onlineParser, offlineSize, onlineSize);
                                
            // Generate traces from parsed files
            tg.generate();
            
            // adds signal strength and distance between online entries and APs
            List<TraceEntry> onlineTrace = tg.getOnline();
            for (TraceEntry entry : onlineTrace) 
            {
				for (int i = 0; i < list.size(); i++) 
				{
					if (ssD.size() < (i + 1))
					{
						ssD.add(new ArrayList());
						double ma = 0;
						try
						{
							ma = entry.getSignalStrengthSamples().getAverageSignalStrength(list.get(i).macAddress);
						}
						catch (Exception e)
						{
							continue;
						}
						ssD.get(i).add(ma + ", " + list.get(i).position.distance(entry.getGeoPosition()));
					}
					else
					{
						double ma = 0;
						try
						{
							ma = entry.getSignalStrengthSamples().getAverageSignalStrength(list.get(i).macAddress);
						}
						catch (Exception e)
						{
							continue;
						}
						ssD.get(i).add(ma + ", " + list.get(i).position.distance(entry.getGeoPosition()));
					}
				}
			}
            
            // writes to file
            for	(int i = 0; i < ssD.size(); i++)
            {
	            PrintWriter writerModelGraph = new PrintWriter("SSD_FP_NN" + (i + 1), "UTF-8");
	            writerModelGraph.println("SS, d");
	            for (String target : ssD.get(i))
	            {
	            	writerModelGraph.println(target);
	            }
	            writerModelGraph.close();
            }
        
        
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }	
    }
}