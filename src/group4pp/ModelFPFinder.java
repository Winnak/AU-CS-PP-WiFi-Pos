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
    	String accessPointPath = "data/MU.AP__170012_1.positions";
    	File accessPointFile = new File(accessPointPath);
    	ArrayList<AccessPoint> list = LocUtility.parseAccessPointFile(accessPointFile);
    	for (AccessPoint ap : list)
    	{
    		System.out.println(ap);
    	}
    	
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

            List<TraceEntry> onlineTrace = tg.getOnline();

            List<TraceEntry> offlineTrace = tg.getOffline();
            List<TraceEntry> newTraces = new ArrayList<TraceEntry>();
            HashSet<GeoPosition> hsGP = new HashSet<GeoPosition>();
            List<ArrayList<String>> ssD = new ArrayList<ArrayList<String>>();
            
            for (TraceEntry target : offlineTrace)
            {
            	if (hsGP.add(target.getGeoPosition()))
            	{
            		String traceEntryParseString = "t=" + target.getTimestamp() +
            				";pos=" + target.getGeoPosition().getX() + "," + target.getGeoPosition().getY() + "," + target.getGeoPosition().getZ() +
            				";id=" + target.getId();
            		double c = -33.77;
            		double v = 3.415;
            		
            		for (int i = 0; i < list.size(); i++)
            		{
            			traceEntryParseString += ";" + list.get(i).macAddress + "=" + (-10 * v * Math.log10(target.getGeoPosition().distance(list.get(i).position))+c) + ",2.412E9,3,-96";
            			if ( ssD.size() < i + 1)
            			{
            				ssD.add(new ArrayList());
                			ssD.get(i).add((-10 * v * Math.log10(target.getGeoPosition().distance(list.get(i).position))+c) + ", " + target.getGeoPosition().distance(list.get(i).position));
            			}
            			else
            			{
                			ssD.get(i).add((-10 * v * Math.log10(target.getGeoPosition().distance(list.get(i).position))+c) + ", " + target.getGeoPosition().distance(list.get(i).position));
            			}
            		}
            		TraceEntry te = TraceEntry.fromString(traceEntryParseString);
            		newTraces.add(te);
                	System.out.println(te);
                	System.out.println(target);
            	}
            }
            PrintWriter writerModel = new PrintWriter("model_FP_NN", "UTF-8");
            writerModel.println("estimated pos,true pos");
            for (TraceEntry target : onlineTrace)
            {
                GeoPosition estimate = LocUtility.findPositionOfTraceKNNSS(target, newTraces, 1);
                writerModel.print(estimate.toString());
                writerModel.print(',');
                writerModel.println(target.getGeoPosition());
            }
            writerModel.close();
            
            for	(int i = 0; i < ssD.size(); i++)
            {
	            PrintWriter writerModelGraph = new PrintWriter("model_FP_NN" + (i + 1), "UTF-8");
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
