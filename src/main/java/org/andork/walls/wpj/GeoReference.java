package org.andork.walls.wpj;

import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.UnitizedDouble;

public class GeoReference {
	// enum e_ref
	// {REF_FMTDM=1,REF_FMTDMS=2,REF_FMTMASK=3,REF_LONDIR=4,REF_LATDIR=8};

	public static int FormatDegreesMinutes = 1 << 0;
	public static int FormatDegreesMinutesSeconds = 1 << 1;
	public static int FormatMask = FormatDegreesMinutes | FormatDegreesMinutesSeconds;
	public static int WesternHemisphere = 1 << 2;
	public static int SouthernHemisphere = 1 << 3;

	public byte flags;
	// positive for north, negative for south
	public int zone;
	public UnitizedDouble<Length> northing;
	public UnitizedDouble<Length> easting;
	public UnitizedDouble<Angle> gridConvergence;
	public UnitizedDouble<Length> elevation;
	/**
	 * In the .wpj file this is absolute value, with sign determined by REF_LATDIR,
	 * but fuck that. Here - means west, + means east of prime meridian.
	 */
	public UnitizedDouble<Angle> latitude;
	/**
	 * In the .wpj file this is absolute value, with sign determined by REF_LONDIR,
	 * but fuck that. Here - means south, + means north of prime meridian.
	 */
	public UnitizedDouble<Angle> longitude;
	public int wallsDatumIndex;
	public String datumName;
}
