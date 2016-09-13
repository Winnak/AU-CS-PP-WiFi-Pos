package example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.pi4.locutil.*;
import org.pi4.locutil.trace.TraceEntry;

public class AccessPoint {
	public GeoPosition position;
	public MACAddress macAddress;
	public static AccessPoint Parse(String string)
	{
		AccessPoint ap = new AccessPoint();
		String[] each = string.split(" ");
		if (each.length < 4) return null;
		try {
			ap.macAddress = MACAddress.parse(each[0]);
			ap.position = GeoPosition.parse(each[1] + "," + each[2] + ", " + each[3]);
		}
		catch ( IllegalArgumentException e)
		{
			return null;
		}
		
		return ap;
	}

	@Override
	public String toString() {
		return "AccessPoint [MAC=" + macAddress + ", Pos=" + position + "]";
	}
}
