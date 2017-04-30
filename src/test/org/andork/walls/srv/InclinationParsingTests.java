package org.andork.walls.srv;

import static org.andork.walls.srv.LineParserAssertions.assertThrows;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.UnitizedDouble;
import org.andork.walls.srv.WallsSurveyParser;
import org.junit.Assert;
import org.junit.Test;

public class InclinationParsingTests {

	@Test
	public void testDefaultUnit() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.degrees), 
				new WallsSurveyParser("2.5").inclination(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.gradians), 
				new WallsSurveyParser("2.5").inclination(Angle.gradians));
	}

	@Test
	public void testUnitsSuffixes() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.gradians), 
				new WallsSurveyParser("2.5g").inclination(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.milsNATO), 
				new WallsSurveyParser("2.5m").inclination(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.percentGrade), 
				new WallsSurveyParser("2.5p").inclination(Angle.degrees));
	}
	
	@Test
	public void testNegativeNumbers() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<Angle>(-2.5, Angle.degrees), 
				new WallsSurveyParser("-2.5").inclination(Angle.degrees));
	}
	
	@Test
	public void testPlusSign() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.degrees), 
				new WallsSurveyParser("+2.5").inclination(Angle.degrees));
	}
	
	@Test
	public void testDegreesMinutesSeconds() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<>(5 + (4 + 23 / 60.0) / 60.0, Angle.degrees),
				new WallsSurveyParser("5:4:23").inclination(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(5 + 4 / 60.0, Angle.degrees),
				new WallsSurveyParser("5:4").inclination(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(-5 - (23 / 60.0) / 60.0, Angle.degrees),
				new WallsSurveyParser("-5::23").inclination(Angle.degrees));
		Assert.assertEquals(
				(23.5 / 60.0) / 60.0,
				new WallsSurveyParser("::23.5").inclination(Angle.degrees).doubleValue(Angle.degrees),
				1e-9);
		Assert.assertEquals(
				new UnitizedDouble<>(-4 / 60.0, Angle.degrees),
				new WallsSurveyParser("-:4").inclination(Angle.degrees));
	}

	@Test
	public void testMiscInvalidValues() {
		assertThrows(() -> new WallsSurveyParser("").inclination(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("-").inclination(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("-.").inclination(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser(".").inclination(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("+").inclination(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser(" ").inclination(Angle.degrees));
	}

	@Test
	public void testOutOfRangeValues() {
		assertThrows(() -> new WallsSurveyParser("90.0001").inclination(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("-90.00001").inclination(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("100.0001g").inclination(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("-100.00001g").inclination(Angle.degrees));
	}
}
