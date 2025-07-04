package org.andork.walls.srv;

import java.util.List;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.Unit;
import org.andork.unit.UnitizedNumber;
import org.andork.walls.WallsMessage;
import org.junit.Assert;

public class TestWallsSurveyParser {
	WallsSurveyParser parser = new WallsSurveyParser();

	WallsUnits units;
	Vector vector;
	FixedStation fixedStation;
	List<WallsMessage> messages;
	String comment;
	boolean parsedFlag;
	String[] flagStations;
	String flagName;

	public TestWallsSurveyParser() throws SegmentParseException {
		this(null);
	}

	public TestWallsSurveyParser(String line) throws SegmentParseException {
		parser.setVisitor(new AbstractWallsVisitor() {
			@Override
			public void parsedVector(Vector parsedVector) {
				vector = parsedVector;
			}

			@Override
			public void parsedFixStation(FixedStation station) {
				fixedStation = station;
			}

			@Override
			public void parsedComment(String parsedComment) {
				comment = parsedComment;
			}

			@Override
			public void parsedFlag(List<String> stations, String flag) {
				parsedFlag = true;
				flagStations = stations.toArray(new String[stations.size()]);
				flagName = flag;
			}

			@Override
			public void parsedUnits() {
				units = parser.units.toImmutable();
			}

			@Override
			public void message(WallsMessage message) {
				messages.add(message);
			}
		});
		if (line != null) {
			this.parseLine(line);
		}
	}

	public TestWallsSurveyParser parseLine(String line) throws SegmentParseException {
		units = null;
		vector = null;
		fixedStation = null;
		messages = null;
		comment = null;
		parsedFlag = false;
		flagStations = null;
		flagName = null;
		parser.parseLine(line);
		return this;
	}

	public ExpectedVector expectVector(String line) throws SegmentParseException {
		return this.parseLine(line).expectVector();
	}

	public ExpectedVector expectVector() {
		return new ExpectedVector(this.vector);
	}

	public static class ExpectedVector {
		public Vector vector;

		public ExpectedVector(Vector vector) {
			Assert.assertNotNull(vector);
			this.vector = vector;
		}

		public ExpectedVector from(String expected) {
			Assert.assertEquals(expected, vector.from);
			return this;
		}

		public ExpectedVector resolvedFrom(String expected) {
			Assert.assertEquals(expected, vector.units.processStationName(vector.from));
			return this;
		}

		public ExpectedVector to(String expected) {
			Assert.assertEquals(expected, vector.to);
			return this;
		}

		public ExpectedVector resolvedTo(String expected) {
			Assert.assertEquals(expected, vector.units.processStationName(vector.to));
			return this;
		}

		public ExpectedVector
			rect(UnitizedNumber<Length> north, UnitizedNumber<Length> east, UnitizedNumber<Length> elevation) {
			Assert.assertEquals(north, vector.north);
			Assert.assertEquals(east, vector.east);
			Assert.assertEquals(elevation, vector.elevation);
			return this;
		}

		public ExpectedVector distance(UnitizedNumber<Length> expected) {
			Assert.assertEquals(expected, vector.distance);
			return this;
		}

		public ExpectedVector azimuth(UnitizedNumber<Angle> expectedFrontsight) {
			return this.azimuth(expectedFrontsight, null);
		}

		public ExpectedVector
			azimuth(UnitizedNumber<Angle> expectedFrontsight, UnitizedNumber<Angle> expectedBacksight) {
			Assert.assertEquals(expectedFrontsight, vector.frontsightAzimuth);
			Assert.assertEquals(expectedBacksight, vector.backsightAzimuth);
			return this;
		}

		public ExpectedVector inclination(UnitizedNumber<Angle> expectedFrontsight) {
			return this.inclination(expectedFrontsight, null);
		}

		public ExpectedVector
			inclination(UnitizedNumber<Angle> expectedFrontsight, UnitizedNumber<Angle> expectedBacksight) {
			Assert.assertEquals(expectedFrontsight, vector.frontsightInclination);
			Assert.assertEquals(expectedBacksight, vector.backsightInclination);
			return this;
		}

		public ExpectedVector instrumentHeight(UnitizedNumber<Length> expected) {
			Assert.assertEquals(expected, vector.instrumentHeight);
			return this;
		}

		public ExpectedVector targetHeight(UnitizedNumber<Length> expected) {
			Assert.assertEquals(expected, vector.targetHeight);
			return this;
		}

		public ExpectedVector lruds(
			UnitizedNumber<Length> left,
			UnitizedNumber<Length> right,
			UnitizedNumber<Length> up,
			UnitizedNumber<Length> down) {
			return this.lruds(left, right, up, down, null);
		}

		public ExpectedVector lruds(
			UnitizedNumber<Length> left,
			UnitizedNumber<Length> right,
			UnitizedNumber<Length> up,
			UnitizedNumber<Length> down,
			UnitizedNumber<Angle> facingAzimuth) {
			Assert.assertEquals(left, vector.left);
			Assert.assertEquals(right, vector.right);
			Assert.assertEquals(up, vector.up);
			Assert.assertEquals(down, vector.down);
			Assert.assertEquals(facingAzimuth, vector.lrudFacingAzimuth);
			return this;
		}

		public ExpectedVector varianceOverrides(VarianceOverride horizontal, VarianceOverride vertical) {
			Assert.assertEquals(horizontal, vector.horizontalVariance);
			Assert.assertEquals(vertical, vector.verticalVariance);
			return this;
		}

		public ExpectedVector comment(String expected) {
			Assert.assertEquals(expected, vector.comment);
			return this;
		}

		public ExpectedVector segment(String... expected) {
			Assert
				.assertArrayEquals(
					expected,
					vector.segment != null ? vector.segment.toArray(new String[vector.segment.size()]) : new String[0]);
			return this;
		}
	}

	public ExpectedFlag expectFlag(String line) throws SegmentParseException {
		return this.parseLine(line).expectFlag();
	}

	public ExpectedFlag expectFlag() {
		Assert.assertTrue(parsedFlag);
		return new ExpectedFlag(flagName, flagStations);
	}

	public static class ExpectedFlag {
		public String name;
		public String[] stations;

		public ExpectedFlag(String name, String... stations) {
			this.name = name;
			this.stations = stations;
		}

		public ExpectedFlag name(String expected) {
			Assert.assertEquals(expected, name);
			return this;
		}

		public ExpectedFlag stations(String... expected) {
			Assert.assertArrayEquals(expected, stations);
			return this;
		}
	}

	public ExpectedUnits expectUnits(String line) throws SegmentParseException {
		return this.parseLine(line).expectUnits();
	}

	public ExpectedUnits expectUnits() throws SegmentParseException {
		return new ExpectedUnits(units);
	}

	public static class ExpectedUnits {
		public WallsUnits units;

		public ExpectedUnits(WallsUnits units) {
			Assert.assertNotNull(units);
			this.units = units;
		}

		public ExpectedUnits dUnit(Unit<Length> expected) {
			Assert.assertEquals(expected, units.getDUnit());
			return this;
		}

		public ExpectedUnits aUnit(Unit<Angle> expected) {
			Assert.assertEquals(expected, units.getAUnit());
			return this;
		}

		public ExpectedUnits abUnit(Unit<Angle> expected) {
			Assert.assertEquals(expected, units.getAbUnit());
			return this;
		}

		public ExpectedUnits vUnit(Unit<Angle> expected) {
			Assert.assertEquals(expected, units.getVUnit());
			return this;
		}

		public ExpectedUnits vbUnit(Unit<Angle> expected) {
			Assert.assertEquals(expected, units.getVbUnit());
			return this;
		}
	}

	public ExpectedFixedStation expectFixedStation(String line) throws SegmentParseException {
		return this.parseLine(line).expectFixedStation();
	}

	public ExpectedFixedStation expectFixedStation() {
		return new ExpectedFixedStation(fixedStation);
	}

	public static class ExpectedFixedStation {
		public FixedStation station;

		public ExpectedFixedStation(FixedStation station) {
			Assert.assertNotNull(station);
			this.station = station;
		}

		public ExpectedFixedStation name(String expected) {
			Assert.assertEquals(expected, station.name);
			return this;
		}

		public ExpectedFixedStation north(UnitizedNumber<Length> expected) {
			Assert.assertEquals(expected, station.north);
			return this;
		}

		public ExpectedFixedStation east(UnitizedNumber<Length> expected) {
			Assert.assertEquals(expected, station.east);
			return this;
		}

		public ExpectedFixedStation elevation(UnitizedNumber<Length> expected) {
			Assert.assertEquals(expected, station.elevation);
			return this;
		}

		public ExpectedFixedStation latitude(UnitizedNumber<Angle> expected) {
			Assert.assertEquals(expected, station.latitude);
			return this;
		}

		public ExpectedFixedStation longitude(UnitizedNumber<Angle> expected) {
			Assert.assertEquals(expected, station.longitude);
			return this;
		}

		public ExpectedFixedStation variance(VarianceOverride horizontal, VarianceOverride vertical) {
			Assert.assertEquals(horizontal, station.horizontalVariance);
			Assert.assertEquals(vertical, station.verticalVariance);
			return this;
		}

		public ExpectedFixedStation note(String expected) {
			Assert.assertEquals(expected, station.note);
			return this;
		}

		public ExpectedFixedStation segment(String... expected) {
			Assert
				.assertArrayEquals(
					expected,
					station.segment == null
						? new String[0]
						: station.segment.toArray(new String[station.segment.size()]));
			return this;
		}
	}
}
