package example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pi4.locutil.*;
import org.pi4.locutil.io.*;
import org.pi4.locutil.trace.*;
//import org.pi4.locutil.MACAddress;
//import org.pi4.locutil.io.TraceGenerator;
//import org.pi4.locutil.trace.*;

class MyEntry
{
    public GeoPosition Position;
    public MACAddress Address;
    public List<Double> SignalStrengths;
    
    public int hashCode()
    {
        return Position.hashCode();
    }
}


/**
 * Example of how to use LocUtil
 * 
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
        System.out.println("Offline File: " + offlineFile.getAbsoluteFile());

        File onlineFile = new File(onlinePath);
        Parser onlineParser = new Parser(onlineFile);
        System.out.println("Online File: " + onlineFile.getAbsoluteFile());

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
            double errorMargin = 1;
            
            double averageLength = 0;
            
            for (TraceEntry target : onlineTrace)
            {
                GeoPosition estimate = findPositionOfTrace(tg, target);
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
            /*
            System.out.println(random);
            GeoPosition shortest = findPositionOfTrace(tg, random);

            GeoPosition cheatShort = getCheatPos(tg, random);

            System.out.println();            
            System.out.println(calculateError(random.getGeoPosition(), cheatShort));
            System.out.println("Cheating Position: " + cheatShort);
            
            System.out.println();            
            System.out.println(calculateError(random.getGeoPosition(), shortest));
            

            System.out.println("True Position: " + random.getGeoPosition());
            System.out.println("Shortest position: " + shortest);
            */
        } 
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param tg
     * @param tEntry
     * @return the closest offline point to the trace entry
     */
    private static GeoPosition getCheatPos(TraceGenerator tg, TraceEntry tEntry)
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

    /**
     * @param tg
     * @param targetEntry
     */
    private static GeoPosition findPositionOfTrace(TraceGenerator tg, TraceEntry targetEntry)
    {
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
        
        
        GeoPosition shortest = new GeoPosition(666,666,666,66);
        double currentShortest = 100000000;
        for (Entry<GeoPosition, List<SignalStrengthSamples>> entry : entries.entrySet())
        {
            double distance = getEuclidAveragePosition(M, entry.getValue());
            if (distance < currentShortest)
            {
                currentShortest = distance;
                shortest = entry.getKey();
            }
        }
        
        return shortest;
    }

    private static double calculateError(GeoPosition a, GeoPosition b)
    {
        return a.distance(b);
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
