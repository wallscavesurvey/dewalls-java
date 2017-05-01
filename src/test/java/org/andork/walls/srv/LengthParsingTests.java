package org.andork.walls.srv;

import static org.andork.walls.LineParserAssertions.assertThrows;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Length;
import org.andork.unit.UnitizedDouble;
import org.andork.walls.srv.WallsSurveyParser;
import org.junit.Assert;
import org.junit.Test;

public class LengthParsingTests {
	@Test
	public void testDefaultUnit() throws SegmentParseException {
		Assert.assertEquals(
				Length.meters(2.5),
				new WallsSurveyParser("2.5").length(Length.meters));
		Assert.assertEquals(
				Length.meters(2.5),
				new WallsSurveyParser("2.5").length(Length.meters));
	}
	
	@Test
	public void testUnitsSuffixes() throws SegmentParseException {
		Assert.assertEquals(
				Length.feet(2.5),
				new WallsSurveyParser("2.5f").length(Length.meters));
		Assert.assertEquals(
				Length.inches(2.5),
				new WallsSurveyParser("i2.5").length(Length.meters));
		Assert.assertEquals(
				Length.inches(4).add(Length.feet(2.5)),
				new WallsSurveyParser("2.5i4").length(Length.meters));
	}
	
	public void testNegativeLength() throws SegmentParseException {
		Assert.assertEquals(
				Length.feet(-2.5),
				new WallsSurveyParser("-2.5f").length(Length.meters));
	}
	
	public void testUnsignedNegativeLength() {
		assertThrows(() -> new WallsSurveyParser("-2.5").unsignedLength(Length.meters));
	}
	
	public void testMiscInvalidValues() {
		assertThrows(() -> new WallsSurveyParser("").length(Length.meters));
		assertThrows(() -> new WallsSurveyParser("-").length(Length.meters));
		assertThrows(() -> new WallsSurveyParser("-.").length(Length.meters));
		assertThrows(() -> new WallsSurveyParser(".").length(Length.meters));
		assertThrows(() -> new WallsSurveyParser("+2").length(Length.meters));
		assertThrows(() -> new WallsSurveyParser(" ").length(Length.meters));
	}
}
