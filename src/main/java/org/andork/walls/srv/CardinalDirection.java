package org.andork.walls.srv;
import org.andork.unit.Angle;
import org.andork.unit.UnitizedDouble;

public enum CardinalDirection {
	NORTH(Angle.degrees(0)), EAST(Angle.degrees(90)), SOUTH(
			Angle.degrees(180)), WEST(Angle.degrees(270));

	public final UnitizedDouble<Angle> angle;

	private CardinalDirection(UnitizedDouble<Angle> angle) {
		this.angle = angle;
	}

	private UnitizedDouble<Angle> nonnormQuadrant(CardinalDirection to, UnitizedDouble<Angle> rotation) {
		if (to.ordinal() == (ordinal() + 1) % 4) {
			return angle.add(rotation);
		} else if (ordinal() == (to.ordinal() + 1) % 4) {
			return angle.sub(rotation);
		}
		throw new IllegalArgumentException("invalid from/to combination");
	}

	public UnitizedDouble<Angle> quadrant(CardinalDirection to, UnitizedDouble<Angle> rotation) {
		UnitizedDouble<Angle> result = nonnormQuadrant(to, rotation).mod(Angle.degrees(360));
		return result.doubleValue(result.unit) < 0
				? result.add(Angle.degrees(360))
				: result;
	}
}
