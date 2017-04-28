package org.andork.walls;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.UnitizedDouble;
import org.andork.walls.LineParser.Production;
import org.andork.walls.LineParser.VoidProduction;
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
	
	static void assertThrows(Production<?> p) {
		try {
			p.run();
			Assert.fail("expected production to throw");
		} catch (Exception ex) {
			// ignore
		}
	}
	
	static void assertThrows(VoidProduction p) {
		try {
			p.run();
			Assert.fail("expected production to throw");
		} catch (Exception ex) {
			// ignore
		}
	}
		
	@Test
	public void testPercentGradeForbidden() throws SegmentParseException {
		WallsSurveyParser parser = new WallsSurveyParser("2.5p");
		Assert.assertEquals(
				new UnitizedDouble<>(2.5, Angle.degrees),
				parser.azimuth(Angle.degrees));
		assertThrows(parser::endOfLine);
	}
	
	@Test
	public void testNegativeNumbersInAzimuthOffset() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<>(-2.5, Angle.degrees),
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
				new UnitizedDouble<>(5 + 4 / 60.0, Angle.degrees),
				new WallsSurveyParser("5:4").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(5 + (23 / 60.0) / 60.0, Angle.degrees),
				new WallsSurveyParser("5::23").azimuth(Angle.degrees));
		Assert.assertEquals(
				(23.5 / 60.0) / 60.0,
				new WallsSurveyParser("::23.5").azimuth(Angle.degrees).doubleValue(Angle.degrees),
				1e-9);
		Assert.assertEquals(
				new UnitizedDouble<>(4 / 60.0, Angle.degrees),
				new WallsSurveyParser(":4").azimuth(Angle.degrees));
	}
	
	@Test
	public void testBasicQuadrants() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<>(30, Angle.degrees),
				new WallsSurveyParser("N30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(330, Angle.degrees),
				new WallsSurveyParser("N30W").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(60, Angle.degrees),
				new WallsSurveyParser("E30N").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(120, Angle.degrees),
				new WallsSurveyParser("E30S").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(150, Angle.degrees),
				new WallsSurveyParser("S30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(210, Angle.degrees),
				new WallsSurveyParser("S30W").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(240, Angle.degrees),
				new WallsSurveyParser("W30S").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(300, Angle.degrees),
				new WallsSurveyParser("W30N").azimuth(Angle.degrees));
	}
	
	@Test
	public void testAdvancedQuadrants() throws SegmentParseException {
		Assert.assertEquals(
				new UnitizedDouble<>(30.5, Angle.degrees),
				new WallsSurveyParser("N30.5E").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(329.5, Angle.degrees),
				new WallsSurveyParser("N30.5W").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(30.5 + 30 / 3600.0, Angle.degrees),
				new WallsSurveyParser("N30:30:30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(30 + 30 / 3600.0, Angle.degrees),
				new WallsSurveyParser("N30::30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(30.5 / 3600.0, Angle.degrees),
				new WallsSurveyParser("N::30.5E").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(0.5, Angle.degrees),
				new WallsSurveyParser("N:30E").azimuth(Angle.degrees));
		Assert.assertEquals(
				new UnitizedDouble<>(30, Angle.gradians).in(Angle.degrees),
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
