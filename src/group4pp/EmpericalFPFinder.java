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
        final String outputDir = "bin/output/empirical_FP_KNN";
        
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
            tg = new TraceGenerator(offlineParser, onlineParser, offlineSize, onlineSize);
                                
            // Generate traces from parsed files
            tg.generate();

            List<TraceEntry> onlineTrace = tg.getOnline();
            List<TraceEntry> offlineTrace = tg.getOffline();
            
          
            for (int k = 1; k <= 5; k++)
            {                    
                PrintWriter writer = new PrintWriter(outputDir + k + ".csv", "UTF-8");
                writer.println("estimated pos;true pos");
                
                for (TraceEntry target : onlineTrace)
                {
                    GeoPosition estimate = LocUtility.findPositionOfTraceKNNSS(target, offlineTrace, k);
                    writer.print(estimate.toString());
                    writer.print(';');
                    writer.println(target.getGeoPosition());
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