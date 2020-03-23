package org.andork.walls.wpj;

import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.UnitizedDouble;

public class GeoReference {
	public byte flags;
	// positive for north, negative for south
	public int zone;
	public UnitizedDouble<Length> northing;
	public UnitizedDouble<Length> easting;
	public UnitizedDouble<Angle> gridConvergence;
	public UnitizedDouble<Length> elevation;
	public UnitizedDouble<Angle> latitude;
	public UnitizedDouble<Angle> longitude;
	public int wallsDatumIndex;
	public String datumName;
}
