package org.andork.walls.srv;

import static org.andork.walls.LineParserAssertions.assertThrows;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.UnitizedDouble;
import org.andork.walls.srv.WallsSurveyParser;
import org.junit.Assert;
import org.junit.Test;

public class AzimuthParsingTests {
	@Test
	public void testDefaultUnit() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.degrees), 
				new WallsSurveyParser("2.5").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.gradians), 
				new WallsSurveyParser("2.5").azimuth(Angle.gradians));
	}
	
	@Test
	public void testUnitsSuffixes() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.gradians), 
				new WallsSurveyParser("2.5g").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<Angle>(2.5, Angle.milsNATO), 
				new WallsSurveyParser("2.5m").azimuth(Angle.degrees));
	}

		
	@Test
	public void testPercentGradeForbidden() throws SegmentParseException {
		WallsSurveyParser parser = new WallsSurveyParser("2.5p");
		Assert.assertEquals(
				Angle.degrees(2.5),
				parser.azimuth(Angle.degrees));
		assertThrows(parser::endOfLine);
	}
	
	@Test
	public void testNegativeNumbersInAzimuthOffset() throws SegmentParseException {
		Assert.assertEquals(
				Angle.degrees(-2.5),
				new WallsSurveyParser("-2.5").azimuthOffset(Angle.degrees));
	}
	
	@Test
	public void testNegativeNumbersThrowForAzimuth() {
		assertThrows(() -> new WallsSurveyParser("-2.5").azimuth(Angle.degrees));
	}
	
	@Test
	public void testDegreesMinutesSeconds() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<>(5 + (4 + 23 / 60.0) / 60.0, Angle.degrees),
				new WallsSurveyParser("5:4:23").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(5 + 4 / 60.0),
				new WallsSurveyParser("5:4").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(5 + (23 / 60.0) / 60.0, Angle.degrees),
				new WallsSurveyParser("5::23").azimuth(Angle.degrees));
		Assert.assertEquals(
				(23.5 / 60.0) / 60.0,
				new WallsSurveyParser("::23.5").azimuth(Angle.degrees).doubleValue(Angle.degrees),
				1e-9);
		Assert.assertEquals(
				Angle.degrees(4 / 60.0),
				new WallsSurveyParser(":4").azimuth(Angle.degrees));
	}
	
	@Test
	public void testBasicQuadrants() throws SegmentParseException {
		Assert.assertEquals(
				Angle.degrees(30),
				new WallsSurveyParser("N30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(330),
				new WallsSurveyParser("N30W").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(60),
				new WallsSurveyParser("E30N").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(120),
				new WallsSurveyParser("E30S").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(150),
				new WallsSurveyParser("S30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(210),
				new WallsSurveyParser("S30W").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(240),
				new WallsSurveyParser("W30S").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(300),
				new WallsSurveyParser("W30N").azimuth(Angle.degrees));
	}
	
	@Test
	public void testAdvancedQuadrants() throws SegmentParseException {
		Assert.assertEquals(
				Angle.degrees(30.5),
				new WallsSurveyParser("N30.5E").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(329.5),
				new WallsSurveyParser("N30.5W").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(30.5 + 30 / 3600.0),
				new WallsSurveyParser("N30:30:30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(30 + 30 / 3600.0),
				new WallsSurveyParser("N30::30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(30.5 / 3600.0),
				new WallsSurveyParser("N::30.5E").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.degrees(0.5),
				new WallsSurveyParser("N:30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				Angle.gradians(30).in(Angle.degrees),
				new WallsSurveyParser("N30gE").azimuth(Angle.degrees));
	}
	
	@Test
	public void testMiscInvalidValues() {
		assertThrows(() -> new WallsSurveyParser("").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("-").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("-.").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser(".").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("+2").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser(" ").azimuth(Angle.degrees));
	}
	
	@Test
	public void testOutOfRangeValues() {
		assertThrows(() -> new WallsSurveyParser("360").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("-0.00001").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("400g").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("-0.00001g").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("N90E").azimuth(Angle.degrees));
		assertThrows(() -> new WallsSurveyParser("N100gE").azimuth(Angle.degrees));
		
	}
}
