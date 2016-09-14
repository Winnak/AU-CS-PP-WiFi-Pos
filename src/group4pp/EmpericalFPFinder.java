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
    final static boolean ERROR_MODE = false;

    public static void main(String[] args)
    {
        String outputDir = "bin/output/empirical_FP_NN";
        
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
            
            if(ERROR_MODE) // <- #if where art thou.
            {
                int total = 0;
                int errors = 0;
                double errorMargin = 2;
                
                double averageLength = 0;
                
                for (TraceEntry target : onlineTrace)
                {
                    GeoPosition estimate = LocUtility.findPositionOfTraceKNNSS(target, offlineTrace, 3);
                    double oDist = calculateError(target.getGeoPosition(), estimate);
                    
                    averageLength += oDist;
                    
                    if (oDist > errorMargin)
                    {
                        errors++;
                    }
                    
                    total++;
                }
                averageLength /= total;
                
                System.out.println(averageLength);
                System.out.println(errors + "/" + total);  
            }
            else
            {                
                PrintWriter writer = new PrintWriter(outputDir, "UTF-8");
                writer.println("estimated pos;true pos");
                
                for (TraceEntry target : onlineTrace)
                {
                    GeoPosition estimate = LocUtility.findPositionOfTraceKNNSS(target, offlineTrace, 1);
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
    

    private static double calculateError(GeoPosition groundTruth, GeoPosition estimate)
    {
        return groundTruth.distance(estimate);
    }
}