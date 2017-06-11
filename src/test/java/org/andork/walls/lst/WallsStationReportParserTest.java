package org.andork.walls.lst;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class WallsStationReportParserTest {
	@Test
	public void testParse() throws IOException {
		WallsStationReportParser parser = new WallsStationReportParser();
		InputStream in = getClass().getResourceAsStream("KAUA-NM.LST");
		parser.parse(in, "KAUA-NM.LST");
		List<StationPosition> positions = parser.getReport().stationPositions;
		
		StationPosition pos;
		pos = positions.get(0);
		Assert.assertEquals("", pos.prefix.toString());
		Assert.assertEquals("00'", pos.name.toString());
		Assert.assertEquals(-35.9, pos.east, 0.0);
		Assert.assertEquals(208.78, pos.north, 0.0);
		Assert.assertEquals(-20.74, pos.up, 0.0);
		Assert.assertEquals(null, pos.note);

		pos = positions.get(1);
		Assert.assertEquals("", pos.prefix.toString());
		Assert.assertEquals("00", pos.name.toString());
		Assert.assertEquals(-35.9, pos.east, 0.0);
		Assert.assertEquals(208.78, pos.north, 0.0);
		Assert.assertEquals(-21.67, pos.up, 0.0);
		Assert.assertEquals(null, pos.note);

		pos = positions.get(2);
		Assert.assertEquals("", pos.prefix.toString());
		Assert.assertEquals("5", pos.name.toString());
		Assert.assertEquals(6.31, pos.east, 0.0);
		Assert.assertEquals(17.23, pos.north, 0.0);
		Assert.assertEquals(-18.88, pos.up, 0.0);
		Assert.assertEquals(null, pos.note);

		pos = positions.get(positions.size() - 1);
		Assert.assertEquals("S", pos.prefix.toString());
		Assert.assertEquals("P4W", pos.name.toString());
		Assert.assertEquals(-33.84, pos.east, 0.0);
		Assert.assertEquals(196.68, pos.north, 0.0);
		Assert.assertEquals(-25.5, pos.up, 0.0);
		Assert.assertEquals(null, pos.note);

		pos = positions.get(positions.size() - 9);
		Assert.assertEquals("Well #3", pos.note.toString());
	}
}
