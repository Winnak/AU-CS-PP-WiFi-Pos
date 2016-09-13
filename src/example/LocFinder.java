package example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.pi4.locutil.*;
import org.pi4.locutil.io.*;
import org.pi4.locutil.trace.*;

/**
 * @author hoyjor
 */
public class LocFinder
{

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
        //System.out.println("Offline File: " + offlineFile.getAbsoluteFile());

        File onlineFile = new File(onlinePath);
        Parser onlineParser = new Parser(onlineFile);
        //System.out.println("Online File: " + onlineFile.getAbsoluteFile());

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
            //TraceEntry random = onlineTrace.get((int)(Math.random()*onlineTrace.size()));

            int total = 0;
            int errors = 0;
            double errorMargin = 2;
            
            double averageLength = 0;
            
            for (TraceEntry target : onlineTrace)
            {
                GeoPosition estimate = findPositionOfTraceKNNSS(tg, target, 3);
                //double cDist = calculateError(target.getGeoPosition(), getCheatPos(tg, target));
                double oDist = calculateError(target.getGeoPosition(), estimate);
                //System.out.println(target.getGeoPosition() + ", " + estimate + ", " + oDist + ", " + cDist);
                
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
     * @param tEntry
     * @return the closest offline point to the trace entry
     */
/*    private static GeoPosition getCheatPos(TraceGenerator tg, TraceEntry tEntry)
    {
        GeoPosition cheatShort = new GeoPosition(666, 666, 666, 66);
        double cheatDist = 100000000;
        for (TraceEntry entry : tg.getOffline())
        {
            double thisDist = calculateError(tEntry.getGeoPosition(), entry.getGeoPosition());
            if (thisDist < cheatDist)
            {
                cheatDist = thisDist;
                cheatShort = entry.getGeoPosition();
            }
        }
        return cheatShort;
    }
*/
    
    /**
     * @param tg
     * @param targetEntry
     * @throws Exception 
     */
    private static GeoPosition findPositionOfTraceKNNSS(TraceGenerator tg, TraceEntry targetEntry, int kVal) throws Exception
    {
        if (tg == null)
        {
            throw new Exception("Trace generator was not initialized");
        }
        
        if (tg.getOfflineSetSize() < 1)
        {
            throw new Exception("offline set was too small");
        }
        
        if (kVal < 1)
        {
            throw new Exception("K-value cannot be less than 1");
        }
        
        SignalStrengthSamples M = targetEntry.getSignalStrengthSamples();

        HashMap<GeoPosition, List<SignalStrengthSamples>> entries = new HashMap<GeoPosition, List<SignalStrengthSamples>>();

        List<TraceEntry> offlineTrace = tg.getOffline();
        for (TraceEntry trace : offlineTrace)
        {
            if (!entries.containsKey(trace.getGeoPosition()))
            {
                List<SignalStrengthSamples> values = new ArrayList<SignalStrengthSamples>();
                values.add(trace.getSignalStrengthSamples());
                entries.put(trace.getGeoPosition(), values);
            }
            else
            {
                entries.get(trace.getGeoPosition()).add(trace.getSignalStrengthSamples());
            }
        }
        
        
        LinkedList<Tuple<GeoPosition, Double>> nearby = new LinkedList<>();
        
        for (Entry<GeoPosition, List<SignalStrengthSamples>> entry : entries.entrySet())
        {
            double distance = getEuclidAveragePosition(M, entry.getValue());
            /*if (distance < currentShortest)
            {
                currentShortest = distance;
                shortest = entry.getKey();
            }*/
            
            
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

    private static double calculateError(GeoPosition groundTruth, GeoPosition estimate)
    {
        return groundTruth.distance(estimate);
    }

    private static double getEuclidAveragePosition(SignalStrengthSamples M, List<SignalStrengthSamples> values)
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
        for (MACAddress hotspot : M.getSortedAccessPoints())
        {
            /* Another approach, instead of having a kMissingPenalty, though not as good.
            if(M.getAverageSignalStrength(hotspot) > -30)
            {
                continue;
            }
            */
            
            if (averagePerAddress.containsKey(hotspot))
            {
                double difference = (M.getAverageSignalStrength(hotspot) - averagePerAddress.get(hotspot));
                result += difference * difference;
            }
            else
            {
                double difference = (M.getAverageSignalStrength(hotspot) - kMissingPenalty);
                result += difference * difference;   
            }
        }
        
        return Math.sqrt(result);
    }
}
