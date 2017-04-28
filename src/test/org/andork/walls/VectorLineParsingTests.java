package org.andork.walls;

import java.util.ArrayList;
import java.util.List;


import static org.andork.walls.LineParserAssertions.assertThrows;
import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.UnitizedDouble;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VectorLineParsingTests {
	WallsSurveyParser parser;

    Vector vector;
    FixedStation station;
    List<WallsMessage> messages;
    String comment;

	@Before
	public void setUp() {
		parser = new WallsSurveyParser();
		vector = null;
		station = null;
		messages = new ArrayList<>();
		comment = null;
		parser.setVisitor(new AbstractWallsVisitor() {
			@Override
			public void parsedVector(Vector parsedVector) {
				vector = parsedVector;
			}

			@Override
			public void parsedFixStation(FixedStation station) {
				VectorLineParsingTests.this.station = station;
			}

			@Override
			public void parsedComment(String parsedComment) {
				comment = parsedComment;
			}

			@Override
			public void message(WallsMessage message) {
				messages.add(message);
			}
		});
	}
	
	@Test
	public void basicTest() throws SegmentParseException {
        parser.parseLine("A1 A2 2.5 350 2.3");
        
        Assert.assertEquals("A1", vector.from);
        Assert.assertEquals("A2", vector.to);
        Assert.assertEquals(new UnitizedDouble<>(2.5, Length.meters), vector.distance);
        Assert.assertEquals(new UnitizedDouble<>(350, Angle.degrees), vector.frontsightAzimuth);
        Assert.assertNull(vector.backsightAzimuth);
        Assert.assertEquals(new UnitizedDouble<>(2.3, Angle.degrees), vector.frontsightInclination);
        Assert.assertNull(vector.backsightInclination);
	}
	
	@Test
	public void testStationPrefixes() throws SegmentParseException {
        parser.parseLine(":A1 A2 2.5 350 2.3");
        Assert.assertEquals(":A1", vector.from);

        parser.parseLine("::A1 A2 2.5 350 2.3");
        Assert.assertEquals("::A1", vector.from);

        parser.parseLine(":::A1 A2 2.5 350 2.3");
        Assert.assertEquals(":::A1", vector.from);

        parser.parseLine("q:::A1 A2 2.5 350 2.3");
        Assert.assertEquals("q:::A1", vector.from);

        parser.parseLine(":q::A1 A2 2.5 350 2.3");
        Assert.assertEquals(":q::A1", vector.from);

        parser.parseLine("::q:A1 A2 2.5 350 2.3");
        Assert.assertEquals("::q:A1", vector.from);

        parser.parseLine(":q:q:A1 A2 2.5 350 2.3");
        Assert.assertEquals(":q:q:A1", vector.from);

        parser.parseLine("q::q:A1 A2 2.5 350 2.3");
        Assert.assertEquals("q::q:A1", vector.from);

        parser.parseLine("q:q:q:A1 A2 2.5 350 2.3");
        Assert.assertEquals("q:q:q:A1", vector.from);

        assertThrows(() -> parser.parseLine("::::A1 A2 2.5 350 2.3"));
	}
	
	@Test
	public void backsightsTest() throws SegmentParseException {
        parser.parseLine("A1 A2 2.5 350/349 2.3/2.4");
        
        Assert.assertEquals("A1", vector.from);
        Assert.assertEquals("A2", vector.to);
        Assert.assertEquals(new UnitizedDouble<>(2.5, Length.meters), vector.distance);
        Assert.assertEquals(new UnitizedDouble<>(350, Angle.degrees), vector.frontsightAzimuth);
        Assert.assertEquals(new UnitizedDouble<>(349, Angle.degrees), vector.backsightAzimuth);
        Assert.assertEquals(new UnitizedDouble<>(2.3, Angle.degrees), vector.frontsightInclination);
        Assert.assertEquals(new UnitizedDouble<>(2.4, Angle.degrees), vector.backsightInclination);
	}
	
	@Test
	public void testOmittedFrontsightsAndBacksights() throws SegmentParseException {
        parser.parseLine("A1 A2 2.5 350/-- --/2.4");

        Assert.assertEquals(new UnitizedDouble<>(350, Angle.degrees), vector.frontsightAzimuth);
        Assert.assertNull(vector.backsightAzimuth);
        Assert.assertNull(vector.frontsightInclination);
        Assert.assertEquals(new UnitizedDouble<>(2.4, Angle.degrees), vector.backsightInclination);
	}
	
	@Test
	public void testDistanceCannotBeOmitted() throws SegmentParseException {
		assertThrows(() -> parser.parseLine("A B -- 350 2.5"));
	}
	
	@Test
	public void testDistanceCannotBeNegative() throws SegmentParseException {
		assertThrows(() -> parser.parseLine("A B -0.001 350 2.5"));
	}
	
	@Test
	public void testDUnitAffectsDistance() throws SegmentParseException {
        parser.parseLine("#units d=feet");
        parser.parseLine("A B 2.5 350 4");
        Assert.assertEquals(new UnitizedDouble<>(2.5, Length.feet), vector.distance);
	}
	
	@Test
	public void testSUnitAffectsDistance() throws SegmentParseException {
        parser.parseLine("#units s=feet");
        parser.parseLine("A B 2.5 350 4");
        Assert.assertEquals(new UnitizedDouble<>(2.5, Length.meters), vector.distance);
	}

	@Test
	public void testIncd() throws SegmentParseException {
        parser.parseLine("#units incd=-2");
		// zero-length shots should not be affected
		parser.parseLine("A B 0 350 4");
		// shots long enough should be OK
		parser.parseLine("A B 2.1 350 4");
		assertThrows(() -> parser.parseLine("A B 1 350 4"));
	}
}
