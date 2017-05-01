package org.andork.walls.srv;

import static org.andork.walls.LineParserAssertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.UnitizedDouble;
import org.andork.walls.WallsMessage;
import org.andork.walls.srv.AbstractWallsVisitor;
import org.andork.walls.srv.CtMeasurement;
import org.andork.walls.srv.FixedStation;
import org.andork.walls.srv.MutableWallsUnits;
import org.andork.walls.srv.VarianceOverride;
import org.andork.walls.srv.Vector;
import org.andork.walls.srv.WallsSurveyParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SurveyLineParsingTests {
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
				SurveyLineParsingTests.this.station = station;
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
		Assert.assertEquals(Length.meters(2.5), vector.distance);
		Assert.assertEquals(Angle.degrees(350), vector.frontsightAzimuth);
		Assert.assertNull(vector.backsightAzimuth);
		Assert.assertEquals(Angle.degrees(2.3), vector.frontsightInclination);
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
		Assert.assertEquals(Length.meters(2.5), vector.distance);
		Assert.assertEquals(Angle.degrees(350), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(349), vector.backsightAzimuth);
		Assert.assertEquals(Angle.degrees(2.3), vector.frontsightInclination);
		Assert.assertEquals(Angle.degrees(2.4), vector.backsightInclination);
	}

	@Test
	public void testOmittedFrontsightsAndBacksights() throws SegmentParseException {
		parser.parseLine("A1 A2 2.5 350/-- --/2.4");

		Assert.assertEquals(Angle.degrees(350), vector.frontsightAzimuth);
		Assert.assertNull(vector.backsightAzimuth);
		Assert.assertNull(vector.frontsightInclination);
		Assert.assertEquals(Angle.degrees(2.4), vector.backsightInclination);
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
		Assert.assertEquals(Length.feet(2.5), vector.distance);
	}

	@Test
	public void testSUnitAffectsDistance() throws SegmentParseException {
		parser.parseLine("#units s=feet");
		parser.parseLine("A B 2.5 350 4");
		Assert.assertEquals(Length.meters(2.5), vector.distance);
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

	@Test
	public void testAzimuthCanBeOmittedForVerticalShots() throws SegmentParseException {
		parser.parseLine("A B 2.5 -- 90");
		parser.parseLine("A B 2.5 -- 90/-90");
		parser.parseLine("A B 2.5 -- --/90");
		parser.parseLine("A B 2.5 -- --/-90");
		parser.parseLine("A B 2.5 -- 90/-90");
		parser.parseLine("A B 2.5 --/-- 90");
		parser.parseLine("A B 2.5 -- -90");
		parser.parseLine("A B 2.5 -- 100g");
		parser.parseLine("A B 2.5 -- -100g");
	}

	@Test
	public void testFrontsightAzimuthCanBeOmittedWithoutDashes() throws SegmentParseException {
		parser.parseLine("A B 2.5 /235 0");

	}

	@Test
	public void testAzimuthCantBeOmittedForNonVerticalShots() {
		assertThrows(() -> parser.parseLine("A B 2.5 - 45"));
		assertThrows(() -> parser.parseLine("A B 2.5 -/-- 45"));
	}

	@Test
	public void testAUnit() throws SegmentParseException {
		parser.parseLine("#units a=grads");
		parser.parseLine("A B 1 2/3 4");
		Assert.assertEquals(Angle.gradians(2), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(3), vector.backsightAzimuth);
	}

	@Test
	public void testAbUnit() throws SegmentParseException {
		parser.parseLine("#units ab=grads");
		parser.parseLine("A B 1 2/3 4");
		Assert.assertEquals(Angle.degrees(2), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.gradians(3), vector.backsightAzimuth);
	}

	@Test
	public void testA_AbUnit() throws SegmentParseException {
		parser.parseLine("#units a/ab=grads");
		parser.parseLine("A B 1 2/3 4");
		Assert.assertEquals(Angle.gradians(2), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.gradians(3), vector.backsightAzimuth);
	}

	@Test
	public void testParserWarnsIfAzmFsBsDifferenceExceedsTolerance() throws SegmentParseException {
		messages.clear();
		parser.parseLine("A B 1 1/179 4");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 1/183 4");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 1/184 4");
		Assert.assertEquals(1, messages.size());
		Assert.assertTrue(messages.get(0).message.contains("exceeds"));

		messages.clear();
		parser.parseLine("#units typeab=c");
		parser.parseLine("A B 1 1/3 4");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 1/359 4");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 359/1 4");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("#units typeab=c,5");
		parser.parseLine("A B 1 1/6 4");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 1/7 4");
		Assert.assertEquals(1, messages.size());
		Assert.assertTrue(messages.get(0).message.contains("exceeds"));
	}

	@Test
	public void testVUnit() throws SegmentParseException {
		parser.parseLine("#units v=grads");
		parser.parseLine("A B 1 2 3/4");
		Assert.assertEquals(Angle.gradians(3), vector.frontsightInclination);
		Assert.assertEquals(Angle.degrees(4), vector.backsightInclination);
	}

	@Test
	public void testBacksightInclinationCanBeOmittedWithoutDashes() throws SegmentParseException {
		parser.parseLine("A B 1 2 /3");
	}

	@Test
	public void testVbUnit() throws SegmentParseException {
		parser.parseLine("#units vb=grads");
		parser.parseLine("A B 1 2 3/4");
		Assert.assertEquals(Angle.degrees(3), vector.frontsightInclination);
		Assert.assertEquals(Angle.gradians(4), vector.backsightInclination);
	}

	@Test
	public void testV_VbUnit() throws SegmentParseException {
		parser.parseLine("#units v/vb=grads");
		parser.parseLine("A B 1 2 3/4");
		Assert.assertEquals(Angle.gradians(3), vector.frontsightInclination);
		Assert.assertEquals(Angle.gradians(4), vector.backsightInclination);
	}

	@Test
	public void testParserWarnsIfIncFsBsDifferenceExceedsTolerance() throws SegmentParseException {
		messages.clear();
		parser.parseLine("A B 1 2 4/-6");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 2 4/-2");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 2 4/-7");
		Assert.assertEquals(1, messages.size());
		Assert.assertTrue(messages.get(0).message.contains("exceeds"));

		messages.clear();
		parser.parseLine("#units typevb=c");
		parser.parseLine("A B 1 2 1/3");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 2 1/-1");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("#units typevb=c,5");
		parser.parseLine("A B 1 2 1/6");
		Assert.assertEquals(0, messages.size());

		messages.clear();
		parser.parseLine("A B 1 2 1/7");
		Assert.assertEquals(1, messages.size());
		Assert.assertTrue(messages.get(0).message.contains("exceeds"));
	}

	@Test
	public void basicRectangularVectorTest() throws SegmentParseException {
		parser.parseLine("#units rect");
		parser.parseLine("A B 1 2 3");
		Assert.assertEquals(Length.meters(1), vector.east);
		Assert.assertEquals(Length.meters(2), vector.north);
		Assert.assertEquals(Length.meters(3), vector.elevation);

		assertThrows(() -> parser.parseLine("A B 1 2"));
	}

	@Test
	public void testUpCanBeOmitted() throws SegmentParseException {
		parser.parseLine("#units rect order=ne");
		parser.parseLine("A B 1 2");
		Assert.assertEquals(Length.meters(1), vector.north);
		Assert.assertEquals(Length.meters(2), vector.east);
	}

	@Test
	public void testMeasurementsCanBeReordered() throws SegmentParseException {
		parser.parseLine("#units rect order=nue");
		parser.parseLine("A B 1 2 3");
		Assert.assertEquals(Length.meters(1), vector.north);
		Assert.assertEquals(Length.meters(2), vector.elevation);
		Assert.assertEquals(Length.meters(3), vector.east);
	}

	@Test
	public void testLRUDOnlyLinesCanBeParsed() throws SegmentParseException {
		parser.parseLine("#units rect");
		parser.parseLine("A *1 2 3 4*");
	}

	@Test
	public void testLRUDToStationNameAmbiguity() throws SegmentParseException {
		parser.parseLine("#units rect");
		parser.parseLine("A <1 2 3 4");
		Assert.assertEquals("<1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.east);
		Assert.assertEquals(Length.meters(3), vector.north);
		Assert.assertEquals(Length.meters(4), vector.elevation);
		Assert.assertNull(vector.left);
		Assert.assertNull(vector.right);
		Assert.assertNull(vector.up);
		Assert.assertNull(vector.down);

		parser.parseLine("A *1 2 3 4 *5,6,7,8*");
		Assert.assertEquals("*1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.east);
		Assert.assertEquals(Length.meters(3), vector.north);
		Assert.assertEquals(Length.meters(4), vector.elevation);
		Assert.assertEquals(Length.meters(5), vector.left);
		Assert.assertEquals(Length.meters(6), vector.right);
		Assert.assertEquals(Length.meters(7), vector.up);
		Assert.assertEquals(Length.meters(8), vector.down);
	}

	@Test
	public void testSplayShots() throws SegmentParseException {
		parser.parseLine("A - 2.5 350 5");
		parser.parseLine("- B 2.5 350 5");
		assertThrows(() -> parser.parseLine("- - 2.5 350 5"));
	}

	@Test
	public void testCompassTapeMeasurementsCanBeReordered() throws SegmentParseException {
		parser.parseLine("#units order=avd");
		parser.parseLine("A B 1 2 3");
		Assert.assertEquals(Angle.degrees(1), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(2), vector.frontsightInclination);
		Assert.assertEquals(Length.meters(3), vector.distance);
	}

	@Test
	public void testInclinationCanBeOmittedFromOrder() throws SegmentParseException {
		parser.parseLine("#units order=da");
		parser.parseLine("A B 1 2");
		Assert.assertEquals(Length.meters(1), vector.distance);
		Assert.assertEquals(Angle.degrees(2), vector.frontsightAzimuth);
		Assert.assertNull(vector.frontsightInclination);
	}

	@Test
	public void testBasicInstrumentAndTargetHeights() throws SegmentParseException {
		parser.parseLine("A B 1 2 3 4 5");
		Assert.assertEquals(Length.meters(4), vector.instrumentHeight);
		Assert.assertEquals(Length.meters(5), vector.targetHeight);
	}

	//
	@Test
	public void testInstrumentAndTargetHeightsAreAffectedBySUnit() throws SegmentParseException {
		parser.parseLine("#units s=feet");
		parser.parseLine("A B 1 2 3 4 5");
		Assert.assertEquals(Length.feet(4), vector.instrumentHeight);
		Assert.assertEquals(Length.feet(5), vector.targetHeight);
	}

	@Test
	public void testInstrumentAndTargetHeightsAreNotAffectedByDUnit() throws SegmentParseException {
		parser.parseLine("#units d=feet");
		parser.parseLine("A B 1 2 3 4 5");
		Assert.assertEquals(Length.meters(4), vector.instrumentHeight);
		Assert.assertEquals(Length.meters(5), vector.targetHeight);
	}

	@Test
	public void testInstTargetHeightsWithInclinationOmitted() throws SegmentParseException {
		parser.parseLine("A B 1 2 -- 4 5");
		Assert.assertEquals(Length.meters(4), vector.instrumentHeight);
		Assert.assertEquals(Length.meters(5), vector.targetHeight);
	}

	@Test
	public void testInstTargetHeightsArentAllowedForRectLines() throws SegmentParseException {
		parser.parseLine("#units rect");
		assertThrows(() -> parser.parseLine("A B 1 2 3 4 5"));
	}

	@Test
	public void testVarianceOverridesCantBothBeOmitted() throws SegmentParseException {
		assertThrows(() -> parser.parseLine("A B 1 2 3 (,)"));
	}

	@Test
	public void testOneVarianceOverrideCanBeOmitted() throws SegmentParseException {
		parser.parseLine("A B 1 2 3 (?,)");
		Assert.assertEquals(VarianceOverride.FLOATED, vector.horizontalVariance);
		Assert.assertNull(vector.verticalVariance);

		parser.parseLine("A B 1 2 3 (,?)");
		Assert.assertNull(vector.horizontalVariance);
		Assert.assertEquals(VarianceOverride.FLOATED, vector.verticalVariance);
	}

	@Test
	public void testVarianceOverrideTypes() throws SegmentParseException {
		parser.parseLine("A B 1 2 3 (?,*)");
		Assert.assertEquals(VarianceOverride.FLOATED, vector.horizontalVariance);
		Assert.assertEquals(VarianceOverride.FLOATED_TRAVERSE, vector.verticalVariance);

		parser.parseLine("A B 1 2 3 (1000f,r4.5f)");
		Assert.assertTrue(vector.horizontalVariance instanceof VarianceOverride.Length);
		Assert.assertEquals(Length.feet(1000),
				((VarianceOverride.Length) vector.horizontalVariance).lengthOverride);
		Assert.assertTrue(vector.verticalVariance instanceof VarianceOverride.RMSError);
		Assert.assertEquals(Length.feet(4.5),
				((VarianceOverride.RMSError) vector.verticalVariance).error);
	}

	@Test
	public void testBasicLruds() throws SegmentParseException {
		parser.parseLine("A B 1 2 3 *4,5,6,7*");
		Assert.assertEquals(Length.meters(4), vector.left);
		Assert.assertEquals(Length.meters(5), vector.right);
		Assert.assertEquals(Length.meters(6), vector.up);
		Assert.assertEquals(Length.meters(7), vector.down);

		parser.parseLine("A B 1 2 3 *4 5 6 7*");
		Assert.assertEquals(Length.meters(4), vector.left);
		Assert.assertEquals(Length.meters(5), vector.right);
		Assert.assertEquals(Length.meters(6), vector.up);
		Assert.assertEquals(Length.meters(7), vector.down);

		parser.parseLine("A B 1 2 3 <4 5 6 7>");
		Assert.assertEquals(Length.meters(4), vector.left);
		Assert.assertEquals(Length.meters(5), vector.right);
		Assert.assertEquals(Length.meters(6), vector.up);
		Assert.assertEquals(Length.meters(7), vector.down);

		parser.parseLine("A B 1 2 3 <4,5,6,7>");
		Assert.assertEquals(Length.meters(4), vector.left);
		Assert.assertEquals(Length.meters(5), vector.right);
		Assert.assertEquals(Length.meters(6), vector.up);
		Assert.assertEquals(Length.meters(7), vector.down);
	}

	@Test
	public void testCanOmitLruds() throws SegmentParseException {
		parser.parseLine("A B 1 2 3 <4,-,6,-->");
		Assert.assertEquals(Length.meters(4), vector.left);
		Assert.assertNull(vector.right);
		Assert.assertEquals(Length.meters(6), vector.up);
		Assert.assertNull(vector.down);
	}

	@Test
	public void testNegativeLrudsAllowed() throws SegmentParseException {
		parser.parseLine("A B 1 2 3 *-4,5,-6f,7*");
		Assert.assertEquals(Length.meters(-4), vector.left);
		Assert.assertEquals(Length.meters(5), vector.right);
		Assert.assertEquals(Length.feet(-6), vector.up);
		Assert.assertEquals(Length.meters(7), vector.down);
		Assert.assertEquals(2, messages.size());
	}

	@Test
	public void testCanUnitizeIndividualLruds() throws SegmentParseException {
		parser.parseLine("A B 1 2 3 *4f,5m,6i3,i7*");
		Assert.assertEquals(Length.feet(4), vector.left);
		Assert.assertEquals(Length.meters(5), vector.right);
		Assert.assertEquals(Length.inches(6 * 12 + 3), vector.up);
		Assert.assertEquals(Length.inches(7), vector.down);
	}

	@Test
	public void testSUnitAffectsLruds() throws SegmentParseException {
		parser.parseLine("#units s=feet");
		parser.parseLine("A B 1 2 3 *4,5,6,7*");
		Assert.assertEquals(Length.feet(4), vector.left);
		Assert.assertEquals(Length.feet(5), vector.right);
		Assert.assertEquals(Length.feet(6), vector.up);
		Assert.assertEquals(Length.feet(7), vector.down);
	}

	@Test
	public void testDUnitDoesntAffectLruds() throws SegmentParseException {
		parser.parseLine("#units d=feet");
		parser.parseLine("A B 1 2 3 *4,5,6,7*");
		Assert.assertEquals(Length.meters(4), vector.left);
		Assert.assertEquals(Length.meters(5), vector.right);
		Assert.assertEquals(Length.meters(6), vector.up);
		Assert.assertEquals(Length.meters(7), vector.down);
	}

	@Test
	public void testMalformedLruds() throws SegmentParseException {
		assertThrows(() -> parser.parseLine("A B 1 2 3 *4,5,6,7>"));
		assertThrows(() -> parser.parseLine("A B 1 2 3 <4,5,6,7*"));
	}

	@Test
	public void testMalformedLrudsThatWallsAcceptsInContradictionOfItsDocumentation() throws SegmentParseException {
		parser.parseLine("A B 1 2 3 *4,5 6,7*");
		parser.parseLine("A B 1 2 3 <4,5 6,7>");

		messages.clear();
		parser.parseLine("A B 1 2 3 <4,5,6,>");
		Assert.assertTrue(!messages.isEmpty());

		messages.clear();
		parser.parseLine("A B 1 2 3 <4,5,6>");
		Assert.assertTrue(!messages.isEmpty());

		messages.clear();
		parser.parseLine("A B 1 2 3 <1>");
		Assert.assertTrue(!messages.isEmpty());

		messages.clear();
		parser.parseLine("A B 1 2 3 <>");
		Assert.assertTrue(!messages.isEmpty());

		messages.clear();
		parser.parseLine("A B 1 2 3 < >");
		Assert.assertTrue(!messages.isEmpty());

		messages.clear();
		parser.parseLine("A B 1 2 3 <,>");
		Assert.assertTrue(!messages.isEmpty());
	}

	@Test
	public void testCanChangeLrudOrder() throws SegmentParseException {
		parser.parseLine("#units lrud=from:urld");
		parser.parseLine("A B 1 2 3 *4,5,6,7*");
		Assert.assertEquals(Length.meters(4), vector.up);
		Assert.assertEquals(Length.meters(5), vector.right);
		Assert.assertEquals(Length.meters(6), vector.left);
		Assert.assertEquals(Length.meters(7), vector.down);
	}

	@Test
	public void testLrudStationNameAmbiguity() throws SegmentParseException {
		parser.parseLine("A *1 2 3 4");
		Assert.assertEquals("*1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.distance);
		Assert.assertEquals(Angle.degrees(3), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(4), vector.frontsightInclination);
		Assert.assertNull(vector.left);
		Assert.assertNull(vector.right);
		Assert.assertNull(vector.up);
		Assert.assertNull(vector.down);

		parser.parseLine("A <1 2 3 4");
		Assert.assertEquals("<1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.distance);
		Assert.assertEquals(Angle.degrees(3), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(4), vector.frontsightInclination);
		Assert.assertNull(vector.left);
		Assert.assertNull(vector.right);
		Assert.assertNull(vector.up);
		Assert.assertNull(vector.down);

		parser.parseLine("A <1 2 3 4 (?, ?)");
		Assert.assertEquals("<1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.distance);
		Assert.assertEquals(Angle.degrees(3), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(4), vector.frontsightInclination);
		Assert.assertNull(vector.left);
		Assert.assertNull(vector.right);
		Assert.assertNull(vector.up);
		Assert.assertNull(vector.down);

		parser.parseLine("A *1 2 3 4 *5,6,7,8*");
		Assert.assertEquals("*1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.distance);
		Assert.assertEquals(Angle.degrees(3), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(4), vector.frontsightInclination);
		Assert.assertEquals(Length.meters(5), vector.left);
		Assert.assertEquals(Length.meters(6), vector.right);
		Assert.assertEquals(Length.meters(7), vector.up);
		Assert.assertEquals(Length.meters(8), vector.down);

		parser.parseLine("A *1 2 3 4 4.5 *5,6,7,8*");
		Assert.assertEquals("*1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.distance);
		Assert.assertEquals(Angle.degrees(3), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(4), vector.frontsightInclination);
		Assert.assertEquals(Length.meters(4.5), vector.instrumentHeight);
		Assert.assertEquals(Length.meters(5), vector.left);
		Assert.assertEquals(Length.meters(6), vector.right);
		Assert.assertEquals(Length.meters(7), vector.up);
		Assert.assertEquals(Length.meters(8), vector.down);

		parser.parseLine("A *1 2 3 4 4.5 4.6 *5,6,7,8,9*");
		Assert.assertEquals("*1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.distance);
		Assert.assertEquals(Angle.degrees(3), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(4), vector.frontsightInclination);
		Assert.assertEquals(Length.meters(4.5), vector.instrumentHeight);
		Assert.assertEquals(Length.meters(4.6), vector.targetHeight);
		Assert.assertEquals(Length.meters(5), vector.left);
		Assert.assertEquals(Length.meters(6), vector.right);
		Assert.assertEquals(Length.meters(7), vector.up);
		Assert.assertEquals(Length.meters(8), vector.down);
		Assert.assertEquals(Angle.degrees(9), vector.lrudFacingAzimuth);

		parser.parseLine("A *1 2 3 4 *");
		Assert.assertNull(vector.to);
		Assert.assertNull(vector.distance);
		Assert.assertNull(vector.frontsightAzimuth);
		Assert.assertNull(vector.frontsightInclination);
		Assert.assertEquals(Length.meters(1), vector.left);
		Assert.assertEquals(Length.meters(2), vector.right);
		Assert.assertEquals(Length.meters(3), vector.up);
		Assert.assertEquals(Length.meters(4), vector.down);

		parser.parseLine("A *1 2 3 4 5*");
		Assert.assertNull(vector.to);
		Assert.assertNull(vector.distance);
		Assert.assertNull(vector.frontsightAzimuth);
		Assert.assertNull(vector.frontsightInclination);
		Assert.assertEquals(Length.meters(1), vector.left);
		Assert.assertEquals(Length.meters(2), vector.right);
		Assert.assertEquals(Length.meters(3), vector.up);
		Assert.assertEquals(Length.meters(4), vector.down);
		Assert.assertEquals(Angle.degrees(5), vector.lrudFacingAzimuth);

		parser.parseLine("A *1 2 3 4 5 C*");
		Assert.assertNull(vector.to);
		Assert.assertNull(vector.distance);
		Assert.assertNull(vector.frontsightAzimuth);
		Assert.assertNull(vector.frontsightInclination);
		Assert.assertEquals(Length.meters(1), vector.left);
		Assert.assertEquals(Length.meters(2), vector.right);
		Assert.assertEquals(Length.meters(3), vector.up);
		Assert.assertEquals(Length.meters(4), vector.down);
		Assert.assertEquals(Angle.degrees(5), vector.lrudFacingAzimuth);
		Assert.assertTrue(vector.cFlag);

		parser.parseLine("A <1 2 3 4 <5,6,7,8>");
		Assert.assertEquals("<1", vector.to);
		Assert.assertEquals(Length.meters(2), vector.distance);
		Assert.assertEquals(Angle.degrees(3), vector.frontsightAzimuth);
		Assert.assertEquals(Angle.degrees(4), vector.frontsightInclination);
		Assert.assertEquals(Length.meters(5), vector.left);
		Assert.assertEquals(Length.meters(6), vector.right);
		Assert.assertEquals(Length.meters(7), vector.up);
		Assert.assertEquals(Length.meters(8), vector.down);

		parser.parseLine("A <1 2 3 4 >");
		Assert.assertNull(vector.to);
		Assert.assertNull(vector.distance);
		Assert.assertNull(vector.frontsightAzimuth);
		Assert.assertNull(vector.frontsightInclination);
		Assert.assertEquals(Length.meters(1), vector.left);
		Assert.assertEquals(Length.meters(2), vector.right);
		Assert.assertEquals(Length.meters(3), vector.up);
		Assert.assertEquals(Length.meters(4), vector.down);

		assertThrows(() -> parser.parseLine("A *1 2 3 4 *1 2 3 4"));
		assertThrows(() -> parser.parseLine("A *-- -- -- --"));
	}

	@Test
	public void testInvalidSpacing() throws SegmentParseException {
		assertThrows(() -> parser.parseLine("   A B 1 2 3(?,?)"));
		assertThrows(() -> parser.parseLine("   A B 1 2 3 (?,?)*4,5,6,7*"));
		assertThrows(() -> parser.parseLine("   A B 1 2 3*4,5,6,7*"));
	}

	@Test
	public void testValidSpacing() throws SegmentParseException {
		parser.parseLine("   A B 1 2 3#s blah;test");
		parser.parseLine("   A B 1 2 3 *4,5,6,7*#s blah;test");
		parser.parseLine("   A B 1 2 3 (?,?)#s blah;test");
		parser.parseLine("   A B 1 2 3 (?,?) *4,5,6,7*#s blah;test");
	}

	@Test
	public void testPrefixes() throws SegmentParseException {
		parser.parseLine("#units prefix=a");
		Assert.assertEquals("a:b", parser._units.processStationName("b"));
		Assert.assertEquals("d:b", parser._units.processStationName("d:b"));
		Assert.assertEquals("b", parser._units.processStationName(":b"));

		parser.parseLine("#units prefix2=c");
		Assert.assertEquals("c:a:b", parser._units.processStationName("b"));
		Assert.assertEquals("c::b", parser._units.processStationName(":b"));
		Assert.assertEquals("c:d:b", parser._units.processStationName("d:b"));
		Assert.assertEquals("b", parser._units.processStationName("::b"));
		Assert.assertEquals("b", parser._units.processStationName(":::::b"));

		parser.parseLine("#units prefix1");
		Assert.assertEquals("c::b", parser._units.processStationName("b"));
	}

	@Test
	public void testBasicFixedStations() throws SegmentParseException {
		parser.parseLine("#FIX A1 W97:43:52.5 N31:16:45 323f (?,*) /Entrance #s blah ;dms with ft elevations");
		Assert.assertEquals("A1", station.name);
		Assert.assertEquals(new UnitizedDouble<>(-97 - (43 + 52.5 / 60.0) / 60.0, Angle.degrees), station.longitude);
		Assert.assertEquals(new UnitizedDouble<>(31 + (16 + 45 / 60.0) / 60.0, Angle.degrees), station.latitude);
		Assert.assertEquals(Length.feet(323), station.elevation);
		Assert.assertEquals(VarianceOverride.FLOATED, station.horizontalVariance);
		Assert.assertEquals(VarianceOverride.FLOATED_TRAVERSE, station.verticalVariance);
		Assert.assertEquals("Entrance", station.note);
		Assert.assertEquals(1, station.segment.size());
		Assert.assertEquals("blah", station.segment.get(0));
		Assert.assertEquals("dms with ft elevations", station.comment);

		parser.parseLine("#FIX A4 620775.38 3461050.67 98.45");
		Assert.assertEquals("A4", station.name);
		Assert.assertEquals(Length.meters(620775.38), station.east);
		Assert.assertEquals(Length.meters(3461050.67), station.north);
		Assert.assertEquals(Length.meters(98.45), station.elevation);
	}

	@Test
	public void testFixedStationMeasurementsCanBeReordered() throws SegmentParseException {
		parser.parseLine("#units order=nue");
		parser.parseLine("#fix a 1 2 3");
		Assert.assertEquals(Length.meters(1), station.north);
		Assert.assertEquals(Length.meters(2), station.elevation);
		Assert.assertEquals(Length.meters(3), station.east);
	}

	@Test
	public void testFixedStationMeasurementsAffectedByDUnit() throws SegmentParseException {
		parser.parseLine("#units d=feet");
		parser.parseLine("#fix a 1 2 3");
		Assert.assertEquals(Length.feet(1), station.east);
		Assert.assertEquals(Length.feet(2), station.north);
		Assert.assertEquals(Length.feet(3), station.elevation);
	}

	@Test
	public void testValidFixedStationSpacing() throws SegmentParseException {
		parser.parseLine("#FIX A1 W97:43:52.5 N31:16:45 323f(?,*)/Entrance#s blah;dms with ft elevations");
	}

	@Test
	public void testSaveRestoreAndResetUnits() throws SegmentParseException {
		parser.parseLine("#units lrud=from:urld");
		parser.parseLine("#units save");
		parser.parseLine("#units lrud=from:rldu");
		parser.parseLine("#units save");
		parser.parseLine("#units reset");
		Assert.assertEquals("LRUD", parser._units.lrudOrderString());
		parser.parseLine("#units restore");
		Assert.assertEquals("RLDU", parser._units.lrudOrderString());
		parser.parseLine("#units restore");
		Assert.assertEquals("URLD", parser._units.lrudOrderString());
	}

	@Test
	public void testCantSaveMoreThan10Units() throws SegmentParseException {
		for (int i = 0; i < 10; i++) {
			parser.parseLine("#units save");
		}
		assertThrows(() -> parser.parseLine("#units save"));
	}

	@Test
	public void testCantRestoreMoreThanNumberOfUnitsSaved() throws SegmentParseException {
		for (int i = 0; i < 4; i++) {
			parser.parseLine("#units save");
		}
		for (int i = 0; i < 4; i++) {
			parser.parseLine("#units restore");
		}
		assertThrows(() -> parser.parseLine("#units restore"));
	}

	@Test
	public void testWallsCrazyMacros() throws SegmentParseException {
		parser.parseLine("#units $hello=\"der=vad pre\" $world=\"fix1=hello feet\"");
		Assert.assertEquals("der=vad pre", parser._macros.get("hello"));
		Assert.assertEquals("fix1=hello feet", parser._macros.get("world"));

		parser.parseLine("#units or$(hello)$(world)");

		Assert.assertEquals(3, parser._units.getCtOrder().size());
		Assert.assertTrue(parser._units.getPrefix().size() >= 1);

		Assert.assertEquals(CtMeasurement.INCLINATION, parser._units.getCtOrder().get(0));
		Assert.assertEquals(CtMeasurement.AZIMUTH, parser._units.getCtOrder().get(1));
		Assert.assertEquals(CtMeasurement.DISTANCE, parser._units.getCtOrder().get(2));

		Assert.assertEquals("hello", parser._units.getPrefix().get(0));
		Assert.assertEquals(Length.feet, parser._units.getDUnit());
		Assert.assertEquals(Length.feet, parser._units.getSUnit());

		parser.parseLine("#units $hello $world");
		Assert.assertEquals("", parser._macros.get("hello"));
		Assert.assertEquals("", parser._macros.get("world"));

		assertThrows(() -> parser.parseLine("#units $(undefined)"));
	}

	@Test
	public void testCommentLines() throws SegmentParseException {
		parser.parseLine(";#units invalid=hello");
		Assert.assertEquals("#units invalid=hello", comment);
		vector = null;
		parser.parseLine(";a b 1 2 3");
		Assert.assertEquals("a b 1 2 3", comment);
		Assert.assertNull(vector);
	}
	
	@Test
	public void testBlockComments() throws SegmentParseException {
	    parser.parseLine("#[");
	    parser.parseLine("a b 1 2 3");
	    Assert.assertEquals("a b 1 2 3" ,  comment);
	    Assert.assertNull(vector);
	    parser.parseLine("#units f");
	    Assert.assertEquals(Length.meters ,  parser._units.getDUnit());
	    parser.parseLine("#]");
	    parser.parseLine("a b 1 2 3");
	    Assert.assertEquals("a" ,  vector.from);
	    Assert.assertEquals("b" ,  vector.to);
	    Assert.assertEquals(Length.meters(1) ,  vector.distance);
	    Assert.assertEquals(Angle.degrees(2) ,  vector.frontsightAzimuth);
	    Assert.assertEquals(Angle.degrees(3) ,  vector.frontsightInclination);
	}
	
	@Test
	public void testAverageInclination() throws SegmentParseException {
		MutableWallsUnits units = new MutableWallsUnits();
		units.setTypevbCorrected(true);
	    Assert.assertEquals(Angle.degrees(2) ,  units.averageInclination(Angle.degrees(3), Angle.degrees(1)));
	    Assert.assertEquals(Angle.degrees(2) ,  units.averageInclination(Angle.degrees(1), Angle.degrees(3)));
	    Assert.assertEquals(Angle.degrees(3) ,  units.averageInclination(Angle.degrees(3), null));
	    Assert.assertEquals(Angle.degrees(3) ,  units.averageInclination(null, Angle.degrees(3)));
	
	    units.setTypevbCorrected(false);
	    Assert.assertEquals(Angle.degrees(2) ,  units.averageInclination(Angle.degrees(3), Angle.degrees(-1)));
	    Assert.assertEquals(Angle.degrees(2) ,  units.averageInclination(Angle.degrees(1), Angle.degrees(-3)));
	    Assert.assertEquals(Angle.degrees(-3) ,  units.averageInclination(null, Angle.degrees(3)));
	}
}
