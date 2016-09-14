package group4pp;

import java.io.*;
import java.util.List;

import org.pi4.locutil.GeoPosition;
import org.pi4.locutil.io.TraceGenerator;
import org.pi4.locutil.trace.*;

/**
 * 
 * @author Hoyjor
 */
public class EmpericalFPFinder
{
    public static void main(String[] args)
    {
        final int kInterations = 100;
        final int kKValues = 5;
        final String outputDir = "bin/output/e_";
        
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
            for (int k = 1; k <= kKValues; k++)
            {          
                PrintWriter writer = new PrintWriter(outputDir + k + "_" + ".csv", "UTF-8");
                writer.println("estimated pos;true pos");
                for (int x = 0; x <= kInterations; x++)
                {
                    final int offlineSize = 25;
                    final int onlineSize = 5;
                    tg = new TraceGenerator(offlineParser, onlineParser, offlineSize, onlineSize);
                                        
                    // Generate traces from parsed files
                    tg.generate();
        
                    List<TraceEntry> onlineTrace = tg.getOnline();
                    List<TraceEntry> offlineTrace = tg.getOffline();
    
                    for (TraceEntry target : onlineTrace)
                    {
                        GeoPosition estimate = LocUtility.findPositionOfTraceKNNSS(target, offlineTrace, k);
                        writer.print(estimate.toString());
                        writer.print(';');
                        writer.println(target.getGeoPosition());
                    }
                }
                
                writer.close();
            }  
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}