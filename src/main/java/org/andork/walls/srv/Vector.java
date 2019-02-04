package org.andork.walls.srv;

import static org.andork.unit.UnitizedNumber.isFinite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.andork.segment.Segment;
import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Area;
import org.andork.unit.Length;
import org.andork.unit.Unit;
import org.andork.unit.UnitizedDouble;
import org.andork.unit.UnitizedNumber;
import org.andork.walls.WallsMessage;

public class Vector implements HasVarianceOverrides, HasComment, HasInlineSegment, Cloneable {
	public Segment sourceSegment;
	public String from;
	public String to;
	public UnitizedDouble<Length> distance;
	public UnitizedDouble<Angle> frontsightAzimuth;
	public UnitizedDouble<Angle> backsightAzimuth;
	public UnitizedDouble<Angle> frontsightInclination;
	public UnitizedDouble<Angle> backsightInclination;
	public UnitizedDouble<Length> targetHeight;
	public UnitizedDouble<Length> instrumentHeight;
	public UnitizedDouble<Length> north;
	public UnitizedDouble<Length> east;
	public UnitizedDouble<Length> elevation;
	public VarianceOverride horizontalVariance;
	public VarianceOverride verticalVariance;
	public UnitizedDouble<Length> left;
	public UnitizedDouble<Length> right;
	public UnitizedDouble<Length> up;
	public UnitizedDouble<Length> down;
	public UnitizedDouble<Angle> lrudFacingAzimuth;
	public boolean cFlag;
	public List<String> segment;
	public String comment;
	public Date date;
	public WallsUnits units;
	
	public Vector clone() {
		Vector result;
		try {
			result = (Vector) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		result.segment = segment == null ? null : new ArrayList<>(segment);
		result.date = date == null ? null : (Date) result.date.clone();
		return result;
	}

	public static boolean isVertical(UnitizedDouble<Angle> angle) {
		return angle != null && Math.abs(angle.abs().doubleValue(Angle.degrees) - 90.0) < 1e-6;
	}

	public boolean isVertical() {
		return isVertical(units.averageInclination(frontsightInclination, backsightInclination));
	}
	
	public void processStationNames() {
		from = units.processStationName(from);
		to = units.processStationName(to);
	}

	/**
	 * Derives compass-and-tape measurements from rectangular measurements.
	 */
	public boolean deriveCtFromRect() {
		if (units.getVectorType() != VectorType.RECTANGULAR) {
			return false;
		}
		double northValue = north.doubleValue(north.unit);
		double eastValue = east.doubleValue(north.unit);
		double ne2 = northValue * northValue + eastValue * eastValue;
		UnitizedDouble<Length> ne = new UnitizedDouble<>(Math.sqrt(ne2), north.unit).in(north.unit);
		UnitizedDouble<Length> up = elevation;
		if (!isFinite(up)) {
			up = new UnitizedDouble<>(0, north.unit);
		}
		double upValue = up.doubleValue(north.unit);

		UnitizedDouble<Length> distance = new UnitizedDouble<>(
				Math.sqrt(ne2 + upValue * upValue), north.unit);
		distance = distance.in(units.getDUnit()).sub(units.getIncd());
		UnitizedDouble<Angle> azm = Angle.atan2(east, north).in(units.getAUnit()).sub(units.getInca());
		if (azm.doubleValue(azm.unit) < 0) {
			azm = azm.add(Angle.degrees(360.0));
		}
		frontsightAzimuth = azm;
		frontsightInclination = Angle.atan2(up, ne).in(units.getVUnit()).sub(units.getIncv());
		return true;
	}
	
	public boolean applyHeightCorrections() throws SegmentParseException {
		return this.applyHeightCorrections(null);
	}

	public boolean applyHeightCorrections(WallsVisitor visitor) throws SegmentParseException {
		if (isVertical() || (units.getInch().isZero() && 
				(!isFinite(instrumentHeight) || instrumentHeight.isZero()) && 
				(!isFinite(targetHeight) || targetHeight.isZero()))) {
			return false;
		}
		// get corrected average inclination (default to zero)
		UnitizedDouble<Angle> inc = units.averageInclination(
				frontsightInclination != null ? frontsightInclination.add(units.getIncv()) : null,
				backsightInclination != null ? backsightInclination.add(units.getIncvb()) : null);

		// get corrected distance
		UnitizedDouble<Length> tapeDist = distance.add(units.getIncd());

		// get corrected instrument and target heights (default to zero)
		UnitizedDouble<Length> instrumentHeight = this.instrumentHeight == null
				? null
				: this.instrumentHeight.add(units.getIncs());
		if (!isFinite(instrumentHeight)) {
			instrumentHeight = new UnitizedDouble<>(0, tapeDist.unit);
		}
		UnitizedDouble<Length> targetHeight = this.targetHeight == null
				? null
				: this.targetHeight.add(units.getIncs());
		if (!isFinite(targetHeight)) {
			targetHeight = new UnitizedDouble<>(0, tapeDist.unit);
		}

		UnitizedDouble<Length> stationToStationDist = tapeDist;
		UnitizedDouble<Angle> stationToStationInc = null;

		if (units.getTape().get(0) == TapingMethodMeasurement.STATION &&
				units.getTape().get(1) == TapingMethodMeasurement.STATION &&
				(!isFinite(inc) || inc.isZero())) {

			UnitizedDouble<Length> heightOffset = instrumentHeight.sub(targetHeight);

			if (heightOffset.abs().compareTo(tapeDist.mul(1 + 1e-6)) > 0) {
				tapeDist = heightOffset.abs();
				if (visitor != null) {
					visitor.message(new WallsMessage(
						"error",
						"Change in depth greater than taped distance! Distance assumed to be " + heightOffset.abs() + " (not " + tapeDist + ") to match change",
						sourceSegment));
				}
			}

			if (heightOffset.abs().sub(tapeDist).abs().compareTo(tapeDist.mul(1e-8)) < 0) {
				// vertical shot
				stationToStationInc = heightOffset.isPositive()
						? Angle.degrees(90.0)
						: Angle.degrees(-90.0);
				stationToStationDist = heightOffset.add(units.getInch()).abs();
			} else {
				if (units.getInch().isNonzero()) {
					UnitizedDouble<Length> horizDistance = Area
							.sqrt(Area.square(tapeDist).sub(Area.square(heightOffset)));
					UnitizedDouble<Length> totalHeightOffset = heightOffset.add(units.getInch());
					stationToStationDist = Area
							.sqrt(Area.square(horizDistance).add(Area.square(totalHeightOffset)));
					stationToStationInc = Angle.atan2(totalHeightOffset, horizDistance);
				} else {
					stationToStationInc = Angle.asin(heightOffset.div(tapeDist));
				}
			}
		}
		if (!isFinite(stationToStationInc)) {
			if (!isFinite(inc)) {
				inc = Angle.degrees(0);
			}

			// compute height of tape ends above stations
			UnitizedDouble<Length> tapeFromHeight = units.getTape().get(0) == TapingMethodMeasurement.STATION
					? new UnitizedDouble<>(0, tapeDist.unit) : instrumentHeight;
			UnitizedDouble<Length> tapeToHeight = units.getTape().get(1) == TapingMethodMeasurement.STATION
					? new UnitizedDouble<>(0, tapeDist.unit) : targetHeight;

			// compute height of instrument and target above tape ends
			UnitizedDouble<Length> instHeightAboveTape = instrumentHeight.sub(tapeFromHeight);
			UnitizedDouble<Length> targetHeightAboveTape = targetHeight.sub(tapeToHeight);

			// height change between tape vector and instrument to target vector
			UnitizedDouble<Length> delta = instHeightAboveTape.sub(targetHeightAboveTape);

			if (delta.abs().compareTo(tapeDist) > 0) {
				throw new SegmentParseException(
						"vector is ambiguous because abs(instrument height above tape - target height above tape) > distance.  In this case, there are two possible vectors that fulfill the constraints imposed by the measurements.  Split this shot into two shots (one vertical) to make it unambiguous.",
						sourceSegment);
			}

			// compute instrument to target distance
			// it's difficult to justify this equation in pure text, it requires a geometric proof
			UnitizedDouble<Length> instToTargetDist = Area
					.sqrt(Area.square(tapeDist).sub(Area.square(delta.mul(Angle.cos(inc)))))
					.sub(delta.mul(Angle.sin(inc)));

			// height change between inst to target vector and final corrected vector
			UnitizedDouble<Length> totalDelta = instrumentHeight.sub(targetHeight).add(units.getInch());

			// compute station to station distance and inclination
			stationToStationDist = Area.sqrt(
					Area.square(instToTargetDist.mul(Angle.sin(inc)).add(totalDelta))
							.add(Area.square(instToTargetDist.mul(Angle.cos(inc)))));
			stationToStationInc = Angle.atan2(
					instToTargetDist.mul(Angle.sin(inc)).add(totalDelta),
					instToTargetDist.mul(Angle.cos(inc)));
		}

		// make sure to subtract corrections so that when they are applied later,
		// they will produce the same vector calculated here
		distance = stationToStationDist.sub(units.getIncd());

		if (!isFinite(frontsightInclination) && !isFinite(backsightInclination)) {
			frontsightInclination = stationToStationInc.sub(units.getIncv());
		} else {
			UnitizedDouble<Angle> dInc = stationToStationInc.sub(inc);
			// since we are moving the original vectors by the difference, we don't need to subtract the
			// correction factors -- they're already present
			if (frontsightInclination != null) {
				frontsightInclination = frontsightInclination.add(dInc);
			}
			if (backsightInclination != null) {
				backsightInclination = backsightInclination.add(dInc);
			}
		}

		// clear out the instrument and target heights, since the vector is now fully determined by the
		// distance and inclination
		instrumentHeight = null;
		targetHeight = null;

		return true;
	}

	@Override
	public void setVerticalVarianceOverride(VarianceOverride override) {
		verticalVariance = override;
	}

	@Override
	public void setHorizontalVarianceOverride(VarianceOverride override) {
		horizontalVariance = override;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public void setSegment(List<String> segment) {
		this.segment = segment;
	}

	public boolean hasAzimuth() {
		return frontsightAzimuth != null || backsightAzimuth != null;
	}
	
	public boolean isSplay() {
		return (from == null) != (to == null);
	}
}
