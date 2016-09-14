package group4pp;

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
public class ModelFPFinder
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        final String outputDir = "bin/output/model_FP_KNN";
        
        // Construct parser for APs
        final String accessPointPath = "data/MU.AP__170012_1.positions";
    	File accessPointFile = new File(accessPointPath);
    	ArrayList<AccessPoint> list = LocUtility.parseAccessPointFile(accessPointFile);
    	/*for (AccessPoint ap : list)
    	{
    		System.out.println(ap);
    	}*/
    	
    	final String offlinePath = "data/MU.1.5meters.offline.trace";
    	final String onlinePath = "data/MU.1.5meters.online.trace";

        // Construct parsers
        File offlineFile = new File(offlinePath);
        Parser offlineParser = new Parser(offlineFile);

        File onlineFile = new File(onlinePath);
        Parser onlineParser = new Parser(onlineFile);

        // Construct trace generator
        TraceGenerator tg;
        try
        {
            final int offlineSize = 25;
            final int onlineSize = 5;

            List<TraceEntry> newTraces = new ArrayList<TraceEntry>();

            // k is the number of nearest neighbors, for each k a file will be created
            for	(int k = 1; k <= 5; k++)
            {
            	
			    PrintWriter writerModel = new PrintWriter(outputDir + k + ".csv", "UTF-8");
			    // number of times we reset the random entries, this will create a longer file
	            for (int kIterations = 1; kIterations <= 10; kIterations++)
	            {
		            tg = new TraceGenerator(offlineParser, onlineParser, offlineSize, onlineSize);
				    
		            // Generate traces from parsed files
		            tg.generate();
		
		            List<TraceEntry> onlineTrace = tg.getOnline();
		
		            List<TraceEntry> offlineTrace = tg.getOffline();
		            HashSet<GeoPosition> hsGP = new HashSet<GeoPosition>();
		            // A list for signal strength and distance og calculated points
		            //List<ArrayList<String>> ssD = new ArrayList<ArrayList<String>>();
		            
		            // calculates new entries using a model
		            for (TraceEntry target : offlineTrace)
		            {
		            	if (hsGP.add(target.getGeoPosition()))
		            	{
		            		String traceEntryParseString = "t=" + target.getTimestamp() +
		            				";pos=" + target.getGeoPosition().getX() + "," + target.getGeoPosition().getY() + "," + target.getGeoPosition().getZ() +
		            				";id=" + target.getId();
		            		final double c = -33.77;
		            		final double v = 3.415;
		            		
		            		for (int i = 0; i < list.size(); i++)
		            		{
		            			traceEntryParseString += ";" + list.get(i).macAddress + "=" + (-10 * v * Math.log10(target.getGeoPosition().distance(list.get(i).position))+c) + ",2.412E9,3,-96";
		            			// this comment is to ideal signal strength and distance to calculated points
		            			/*if ( ssD.size() < i + 1)
		            			{
		            				ssD.add(new ArrayList());
		                			ssD.get(i).add((-10 * v * Math.log10(target.getGeoPosition().distance(list.get(i).position))+c) + ", " + target.getGeoPosition().distance(list.get(i).position));
		            			}
		            			else
		            			{
		                			ssD.get(i).add((-10 * v * Math.log10(target.getGeoPosition().distance(list.get(i).position))+c) + ", " + target.getGeoPosition().distance(list.get(i).position));
		            			}*/
		            		}
		            		TraceEntry te = TraceEntry.fromString(traceEntryParseString);
		            		newTraces.add(te);
		            	}
		            }
			        // makes file with estimated position and true position og online points  
				    writerModel.println("estimated pos;true pos");
		            for (TraceEntry target : onlineTrace)
		            {
		                GeoPosition estimate = LocUtility.findPositionOfTraceKNNSS(target, newTraces, k);
		                writerModel.print(estimate.toString());
		                writerModel.print(';');
		                writerModel.println(target.getGeoPosition());
		            }
		            writerModel.close();
	            }
	            
	        // makes file with signal strength and distance for calculated points    
            /*for	(int i = 0; i < ssD.size(); i++)
            {
	            PrintWriter writerModelGraph = new PrintWriter(outputDir + (i + 1), "UTF-8");
	            writerModelGraph.println("SS, d");
	            for (String target : ssD.get(i))
	            {
	            	writerModelGraph.println(target);
	            }
	            writerModelGraph.close();
            }*/
            
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
