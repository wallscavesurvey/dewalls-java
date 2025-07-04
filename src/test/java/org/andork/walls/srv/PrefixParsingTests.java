package org.andork.walls.srv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.walls.WallsMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrefixParsingTests {
	WallsSurveyParser parser;

	Vector vector;
	List<Vector> vectors;
	FixedStation fixedStation;
	List<WallsMessage> messages;
	String comment;

	@Before
	public void setUp() {
		parser = new WallsSurveyParser();
		vector = null;
		fixedStation = null;
		messages = new ArrayList<>();
		vectors = new ArrayList<>();
		comment = null;
		parser.setVisitor(new AbstractWallsVisitor() {
			@Override
			public void parsedVector(Vector parsedVector) {
				vector = parsedVector;
				vectors.add(parsedVector);
			}

			@Override
			public void parsedFixStation(FixedStation station) {
				PrefixParsingTests.this.fixedStation = station;
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
		parser.parseLine("#Prefix foo");
		parser.parseLine("A1 A2 2.5 350 2.3");
		Vector a2 = vector;
		parser.parseLine("A2 A3 2.5 350 2.3");
		Vector a3 = vector;

		parser.parseLine("#Prefix bar");
		parser.parseLine("A3 A4 2.5 350 2.3");
		Vector a4 = vector;
		parser.parseLine("A4 A5 2.5 350 2.3");
		Vector a5 = vector;

		Assert.assertEquals(Arrays.asList("foo"), a2.units.getPrefix());
		Assert.assertEquals(Arrays.asList("foo"), a3.units.getPrefix());
		Assert.assertEquals(Arrays.asList("bar"), a4.units.getPrefix());
		Assert.assertEquals(Arrays.asList("bar"), a5.units.getPrefix());
	}

	@Test
	public void issueTest() throws SegmentParseException {
		parser.parseLine("#Units decl=0d Order=DAV D=Feet A=Deg S=Feet ;Format: QDDDLRUDLADN");
		parser.parseLine("#Date 1986-11-29");
		parser.parseLine("#Prefix BBB63");
		parser.parseLine("BBB63:RA-66 BB95:MR31 0 0 0");

		parser.parseLine("#Prefix BBB64");
		parser.parseLine("#Date 1986-12-01");
		parser.parseLine("RC-57 BB125:LU16 0 0 0");
		parser.parseLine("RC-71 BB30:S1 0 0 0");
		parser.parseLine("BBB63:RA-66 RC-1 50 N85.5W -15 <12,40,6.5,0>");
		parser.parseLine("RC-1 RC-2 50 S60W 0 <20,2,6,0>");
		parser.parseLine("RC-2 RC-3 50 N89.5W 0 <10,10,3,0>");
		
		for (Vector vector : vectors) {
			System.out.println(vector.units.processStationName(vector.from) + "\t-\t" + vector.units.processStationName(vector.to));
		}
	}
}
