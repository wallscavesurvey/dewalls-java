package org.andork.walls.srv;

import static org.andork.walls.LineParserAssertions.assertThrows;

import java.util.List;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.UnitType;
import org.andork.unit.UnitizedDouble;
import org.junit.Assert;
import org.junit.Test;

public class ApplyHeightCorrectionsTest {
	<T extends UnitType<T>> UnitizedDouble<T> usq(UnitizedDouble<T> u) {
		return new UnitizedDouble<>(u.doubleValue(u.unit) * u.doubleValue(u.unit), u.unit);
	}

	<T extends UnitType<T>> UnitizedDouble<T> usqrt(UnitizedDouble<T> u) {
		return new UnitizedDouble<>(Math.sqrt(u.doubleValue(u.unit)), u.unit);
	}

	void testInstance(UnitizedDouble<Length> instY, UnitizedDouble<Length> targetY,
			UnitizedDouble<Length> fromY, UnitizedDouble<Length> toY,
			UnitizedDouble<Length> horizDist, UnitizedDouble<Length> inch,
			List<TapingMethodMeasurement> tape) throws SegmentParseException {
		MutableWallsUnits units = new MutableWallsUnits();
		units.setInch(inch);

		units.setIncd(Length.inches(1));
		units.setIncs(Length.inches(1));
		units.setIncv(Angle.degrees(2));
		units.setTape(tape);

		//	    INFO( "instY: " << instY );
		//	    INFO( "targetY: " << targetY );
		//	    INFO( "fromY: " << fromY );
		//	    INFO( "toY: " << toY );
		//	    INFO( "horizDist: " << horizDist );
		//	    INFO( "inch: " << inch );

		UnitizedDouble<Length> tapeFromY = tape.get(0) == TapingMethodMeasurement.INSTRUMENT_HEIGHT ? instY : fromY;
		UnitizedDouble<Length> tapeToY = tape.get(1) == TapingMethodMeasurement.TARGET_HEIGHT ? targetY : toY;

		Vector vector = new Vector();
		vector.distance = usqrt(usq(horizDist).add(usq(tapeToY.sub(tapeFromY)))).sub(units.getIncd());
		vector.frontsightAzimuth = Angle.degrees(0).sub(units.getInca());
		if (horizDist.isNonzero() && targetY != instY) {
			vector.frontsightInclination = Angle.atan2(targetY.sub(instY), horizDist.abs()).sub(units.getIncv());
		}
		vector.instrumentHeight = instY.sub(fromY).sub(units.getIncs());
		vector.targetHeight = targetY.sub(toY).sub(units.getIncs());
		vector.units = units.toImmutable();

		UnitizedDouble<Length> expectedDist = usqrt(usq(toY.add(inch).sub(fromY)).add(usq(horizDist)));
		UnitizedDouble<Angle> expectedInc = Angle.atan2(toY.add(inch).sub(fromY), horizDist);

		UnitizedDouble<Length> instHeightAboveTape = instY.sub(tapeFromY);
		UnitizedDouble<Length> targetHeightAboveTape = targetY.sub(tapeToY);

		boolean isDiveShot = tape.get(0) == TapingMethodMeasurement.STATION &&
				tape.get(1) == TapingMethodMeasurement.STATION &&
				(!UnitizedDouble.isFinite(vector.frontsightInclination) || vector.frontsightInclination.isZero());

		if (!isDiveShot && instHeightAboveTape.sub(targetHeightAboveTape).abs().compareTo(vector.distance) > 0) {
			assertThrows(() -> vector.applyHeightCorrections());
		} else {
			vector.applyHeightCorrections();
			Assert.assertEquals(
					vector.distance.add(units.getIncd()).doubleValue(Length.meters),
					expectedDist.doubleValue(Length.meters), 1e-9);
			Assert.assertEquals(
					vector.frontsightInclination.add(units.getIncv()).doubleValue(Angle.degrees),
					expectedInc.doubleValue(Angle.degrees), 1e-9);
		}
	}

	void testInstance(UnitizedDouble<Length> instY, UnitizedDouble<Length> targetY, UnitizedDouble<Length> fromY,
			UnitizedDouble<Length> toY, UnitizedDouble<Length> horizDist, UnitizedDouble<Length> inch) throws SegmentParseException {
		if (horizDist.isNonzero()) {
			testInstance(instY, targetY, fromY, toY, horizDist, inch, WallsSurveyParser.tapingMethods.get("it"));
			testInstance(instY, targetY, fromY, toY, horizDist, inch, WallsSurveyParser.tapingMethods.get("is"));
			testInstance(instY, targetY, fromY, toY, horizDist, inch, WallsSurveyParser.tapingMethods.get("st"));
		}
		testInstance(instY, targetY, fromY, toY, horizDist, inch, WallsSurveyParser.tapingMethods.get("ss"));
	}
	
	@Test
	public void testVerticalDiveShotsWithoutInch() throws SegmentParseException {
        testInstance(Length.meters(0), Length.meters(0), Length.meters(-8), Length.meters(-3), Length.meters(0), Length.meters(0));
        testInstance(Length.meters(0), Length.meters(0), Length.meters(-3), Length.meters(-8), Length.meters(0), Length.meters(0));
    }
	@Test
	public void testVerticalDiveShotsWithInch() throws SegmentParseException {
        testInstance(Length.meters(0), Length.meters(0), Length.meters(-8), Length.meters(-3), Length.meters(0), Length.meters(2));
        testInstance(Length.meters(0), Length.meters(0), Length.meters(-3), Length.meters(-8), Length.meters(0), Length.meters(2));
    }
	@Test
	public void testNearVerticalDiveShotsWithoutInch() throws SegmentParseException {
        testInstance(Length.meters(0), Length.meters(0), Length.meters(-3), Length.meters(-18), Length.meters(0.5), Length.meters(0), WallsSurveyParser.tapingMethods.get("ss"));
        testInstance(Length.meters(0), Length.meters(0), Length.meters(-18), Length.meters(-3), Length.meters(0.5), Length.meters(0), WallsSurveyParser.tapingMethods.get("ss"));
    }
	@Test
	public void testNearVerticalDiveShotsWithInch() throws SegmentParseException {
        testInstance(Length.meters(0), Length.meters(0), Length.meters(-3), Length.meters(-18), Length.meters(0.5), Length.meters(2), WallsSurveyParser.tapingMethods.get("ss"));
        testInstance(Length.meters(0), Length.meters(0), Length.meters(-18), Length.meters(-3), Length.meters(0.5), Length.meters(2), WallsSurveyParser.tapingMethods.get("ss"));
    }
	@Test
	public void testVariousShots() throws SegmentParseException {
        testInstance(Length.meters(3), Length.meters(8), Length.meters(2), Length.meters(4), Length.meters(7), Length.meters(2));
        testInstance(Length.meters(3), Length.meters(8), Length.meters(2), Length.meters(9), Length.meters(7), Length.meters(2));
        testInstance(Length.meters(3), Length.meters(8), Length.meters(4), Length.meters(7), Length.meters(7), Length.meters(2));
        testInstance(Length.meters(3), Length.meters(8), Length.meters(0), Length.meters(0), Length.meters(7), Length.meters(2));
    }
	@Test
	public void testRidiculousShots() throws SegmentParseException {
        testInstance(Length.meters(3), Length.meters(8), Length.meters(4), Length.meters(68), Length.meters(7), Length.meters(2));
        testInstance(Length.meters(3), Length.meters(8), Length.meters(68), Length.meters(68), Length.meters(7), Length.meters(2));
        testInstance(Length.meters(3), Length.meters(58), Length.meters(2), Length.meters(-62), Length.meters(7), Length.meters(2));
    }
}
