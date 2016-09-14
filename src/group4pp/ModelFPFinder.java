package group4pp;

import java.io.File;
import java.util.*;

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
        // Der var vist også noget her
        
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
            
            // HER MICHAEL
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    // Og kopier de metoder *DU* har skrevet ind her:
}
