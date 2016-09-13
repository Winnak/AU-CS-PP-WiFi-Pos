package example;

import org.pi4.locutil.*;

public class AccessPoint {
	public GeoPosition position;
	public MACAddress macaddress;
	public static AccessPoint Parse(String string)
	{
		AccessPoint ap = new AccessPoint();
		String[] each = string.split(" ");
		if (each.length < 3) throw new IllegalArgumentException("Invalid format.");
		ap.macaddress = MACAddress.parse(each[0]);
		ap.position = GeoPosition.parse(each[1] + "," + each[2]);
		
		return ap;
	}
}
