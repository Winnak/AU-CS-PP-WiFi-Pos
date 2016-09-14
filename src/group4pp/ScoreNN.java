package group4pp;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.pi4.locutil.GeoPosition;

/**
 * 
 * @author Hoyjor
 */
public class ScoreNN
{

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("No file specified (note: piping not implemented) Example:");
            System.out.println("\t\"ScoreNN emperical_FP_KNN3.csv\"");
            
            return;
        }
        
        final String outputDir = "bin/output/ScoreNN-";
        
        Path path = FileSystems.getDefault().getPath(args[0]);
        System.out.println("Using " + args[0] + " as the chosen estimate/truth file");
        
        try
        {
            List<String> rows = Files.readAllLines(path);
            List<Double> errors = new ArrayList<Double>(rows.size());
            
            for (String row : rows)
            {
                String[] columns = row.split(";");
                if (columns.length != 2)
                {
                    continue;
                }

                try 
                {                    
                    GeoPosition estimate = GeoPosition.parse(columns[0].substring(1, columns[0].length() - 1));
                    GeoPosition groundTruth = GeoPosition.parse(columns[1].substring(1, columns[1].length() - 1));
                    errors.add(calculateError(groundTruth, estimate));
                }
                catch(IllegalArgumentException e)
                {
                    //e.printStackTrace();
                }
            }
            
            Collections.sort(errors);

            String filename = outputDir + args[0].replaceAll("[^a-zA-Z0-9\\._]+", "_") + ".csv";
            
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            for(int i = 0; i < errors.size(); i++)
            {
                writer.println(errors.get(i));
            }
            writer.close();
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

/*
if(ERROR_MODE) // <- #if where art thou.
{
    int total = 0;
    int errors = 0;
    final double errorMargin = 2;
    
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
*/