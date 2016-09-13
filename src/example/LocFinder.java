package example;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import org.pi4.locutil.*;
import org.pi4.locutil.io.*;
import org.pi4.locutil.trace.*;

/**
 * @author hoyjor
 */
public class LocFinder
{
    final static boolean ERROR_MODE = false;

    /**
     * Execute example
     * 
     * @param args
     */
    public static void main(String[] args)
    {
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
            
            if(ERROR_MODE) // <- #if where art thou.
            {
                int total = 0;
                int errors = 0;
                double errorMargin = 2;
                
                double averageLength = 0;
                
                for (TraceEntry target : onlineTrace)
                {
                    GeoPosition estimate = findPositionOfTraceKNNSS(target, tg.getOffline(), 3);
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
            
            PrintWriter writer = new PrintWriter("empirical_FP_NN", "UTF-8");
            writer.println("estimated pos,true pos");
            
            for (TraceEntry target : onlineTrace)
            {
                GeoPosition estimate = findPositionOfTraceKNNSS(target, tg.getOffline(), 1);
                writer.print(estimate.toString());
                writer.print(',');
                writer.println(target.getGeoPosition());
            }
            writer.close();
        } 
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param tg
     * @param targetEntry
     * @throws Exception 
     */
    private static GeoPosition findPositionOfTraceKNNSS(TraceEntry targetEntry, List<TraceEntry> offlineTraces, int kVal) throws Exception
    {
        if (offlineTraces == null)
        {
            throw new Exception("offline traces were not instantiated");
        }
        
        if (offlineTraces.size() < 1)
        {
            throw new Exception("offline set was too small");
        }
        
        if (kVal < 1)
        {
            throw new Exception("K-value cannot be less than 1");
        }
        
        SignalStrengthSamples targetSignalStrength = targetEntry.getSignalStrengthSamples();

        HashMap<GeoPosition, List<SignalStrengthSamples>> uniqueOfflineSamples = new HashMap<GeoPosition, List<SignalStrengthSamples>>();

        for (TraceEntry trace : offlineTraces)
        {
            if (!uniqueOfflineSamples.containsKey(trace.getGeoPosition()))
            {
                List<SignalStrengthSamples> values = new ArrayList<SignalStrengthSamples>();
                values.add(trace.getSignalStrengthSamples());
                uniqueOfflineSamples.put(trace.getGeoPosition(), values);
            }
            else
            {
                uniqueOfflineSamples.get(trace.getGeoPosition()).add(trace.getSignalStrengthSamples());
            }
        }
        
        
        LinkedList<Tuple<GeoPosition, Double>> nearby = findNearestNeighbors(kVal, targetSignalStrength, uniqueOfflineSamples);
        
        return averageNeighbors(nearby);
    }

    /**
     * @param nearby
     * @return
     */
    private static GeoPosition averageNeighbors(LinkedList<Tuple<GeoPosition, Double>> nearby)
    {
        double x = 0, y = 0, z = 0;
        for (Tuple<GeoPosition, Double> closePos : nearby)
        {
            x += closePos.Item1.getX();
            y += closePos.Item1.getY();
            z += closePos.Item1.getZ();
        }
        x /= nearby.size();
        y /= nearby.size();
        z /= nearby.size();
        
        return new GeoPosition(x, y, z);
    }

    /**
     * @param kVal
     * @param currentSignature
     * @param offlineSamples
     * @return
     */
    private static LinkedList<Tuple<GeoPosition, Double>> findNearestNeighbors(int kVal,
            SignalStrengthSamples currentSignature, HashMap<GeoPosition, List<SignalStrengthSamples>> offlineSamples)
    {
        LinkedList<Tuple<GeoPosition, Double>> nearby = new LinkedList<>();
        
        for (Entry<GeoPosition, List<SignalStrengthSamples>> entry : offlineSamples.entrySet())
        {
            double distance = getEuclidAveragePosition(currentSignature, entry.getValue());
            
            // 7 -> [6, 10, 16] (kVal = 3), checks 6, check 10, inserts at that spot, removes the last excess.
            for (int i = 0; i < kVal; i++)
            {
                if (i < nearby.size())
                {
                    Tuple<GeoPosition, Double> current = nearby.get(i);

                    if (distance < current.Item2)
                    {
                        nearby.add(i, new Tuple<GeoPosition, Double>(entry.getKey(), distance));
                        if (nearby.size() > kVal)
                        {
                            nearby.removeLast();
                        }
                        break;
                    }
                }
                else 
                {
                    nearby.addLast(new Tuple<GeoPosition, Double>(entry.getKey(), distance));
                    break;
                }
            }
        }
        return nearby;
    }

    private static double calculateError(GeoPosition groundTruth, GeoPosition estimate)
    {
        return groundTruth.distance(estimate);
    }

    private static double getEuclidAveragePosition(SignalStrengthSamples targetSignalStrengths, List<SignalStrengthSamples> values)
    {
        Map<MACAddress, Tuple<Double, Integer>> totalPerAddress = new HashMap<MACAddress, Tuple<Double, Integer>>();
        for (SignalStrengthSamples samples : values)
        {
            for (MACAddress address : samples.getSortedAccessPoints())
            {
                if (totalPerAddress.containsKey(address))
                {
                    Tuple<Double, Integer> foo = totalPerAddress.get(address);
                    foo.Item1 += samples.getAverageSignalStrength(address);
                    foo.Item2++;
                    totalPerAddress.put(address, foo);
                }
                else 
                {
                    Tuple<Double, Integer> foo = new Tuple<Double, Integer>(samples.getAverageSignalStrength(address), 1);
                    totalPerAddress.put(address, foo);   
                }
            }
        }

        Map<MACAddress, Double> averagePerAddress = new HashMap<MACAddress, Double>();
        for (Entry<MACAddress, Tuple<Double, Integer>> iter : totalPerAddress.entrySet())
        {
            averagePerAddress.put(iter.getKey(), iter.getValue().Item1 / (double)iter.getValue().Item2);
        }
                
        final double kMissingPenalty = -100;
        
        double result = 0;
        for (MACAddress hotspot : targetSignalStrengths.getSortedAccessPoints())
        {
            /* Another approach, instead of having a kMissingPenalty, though not as good.
            if(M.getAverageSignalStrength(hotspot) > -30)
            {
                continue;
            }
            */
            
            if (averagePerAddress.containsKey(hotspot))
            {
                double difference = (targetSignalStrengths.getAverageSignalStrength(hotspot) - averagePerAddress.get(hotspot));
                result += difference * difference;
            }
            else
            {
                double difference = (targetSignalStrengths.getAverageSignalStrength(hotspot) - kMissingPenalty);
                result += difference * difference;   
            }
        }
        
        return Math.sqrt(result);
    }
}
