package org.pi4.locutil.trace.positionfilter;

import org.pi4.locutil.GeoPosition;

public class PositionFilterAllowAll implements PositionFilter {

	@Override
	public boolean contains(GeoPosition pos) {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

}
